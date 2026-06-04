package com.zy.testpilotai.testcase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.common.utils.JsonExtractUtils;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.vo.RagContextVO;
import com.zy.testpilotai.knowledge.service.KnowledgeBaseService;
import com.zy.testpilotai.llm.chat.LlmClient;
import com.zy.testpilotai.project.service.ProjectService;
import com.zy.testpilotai.testcase.mapper.TestCaseGenerateTaskMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseMapper;
import com.zy.testpilotai.testcase.model.dto.TestCaseGenerateRequest;
import com.zy.testpilotai.testcase.model.dto.TestCasePageRequest;
import com.zy.testpilotai.testcase.model.entity.TestCase;
import com.zy.testpilotai.testcase.model.entity.TestCaseGenerateTask;
import com.zy.testpilotai.testcase.model.vo.TestCaseGenerateResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseVO;
import com.zy.testpilotai.testcase.prompt.TestCasePromptBuilder;
import com.zy.testpilotai.testcase.service.TestCaseGenerateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestCaseGenerateServiceImpl implements TestCaseGenerateService {

    private final ProjectService projectService;

    private final KnowledgeBaseService knowledgeBaseService;

    private final LlmClient llmClient;

    private final TestCasePromptBuilder promptBuilder;

    private final TestCaseGenerateTaskMapper taskMapper;

    private final TestCaseMapper testCaseMapper;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseGenerateResultVO generate(TestCaseGenerateRequest request) {
        // 校验项目是否存在，避免无效 projectId 继续执行
        projectService.getById(request.getProjectId());

        String taskId = "tc_" + UUID.randomUUID().toString().replace("-", "");

        TestCaseGenerateTask task = createTask(taskId, request);

        try {
            // 1. 构建知识库检索请求
            KnowledgeSearchRequest searchRequest = new KnowledgeSearchRequest();
            searchRequest.setProjectId(request.getProjectId());
            searchRequest.setVersionNo(request.getVersionNo());
            searchRequest.setModuleCode(request.getModuleCode());
            searchRequest.setQuery(request.getGenerateGoal());
            searchRequest.setTopK(request.getTopK());

            // 2. 构建 RAG 上下文
            RagContextVO ragContext = knowledgeBaseService.buildRagContext(searchRequest);

            if (CollectionUtils.isEmpty(ragContext.getReferences())) {
                throw new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "知识库没有召回相关内容，请先确认 PRD 已解析并构建知识库"
                );
            }

            // 3. 构建 Prompt
            String systemPrompt = promptBuilder.buildSystemPrompt();
            String userPrompt = promptBuilder.buildUserPrompt(request, ragContext);

            // 4. 调用 LLM
            String rawOutput = llmClient.chat(systemPrompt, userPrompt);

            // 5. 保存模型原始输出，方便排查问题
            task.setRawModelOutput(rawOutput);
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);

            // 6. 解析模型输出 JSON
            List<TestCase> testCases = parseAndBuildTestCases(
                    rawOutput,
                    taskId,
                    request
            );

            if (testCases.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.AI_OUTPUT_PARSE_ERROR,
                        "模型没有生成任何测试用例"
                );
            }

            // 7. 入库测试用例
            for (TestCase testCase : testCases) {
                testCaseMapper.insert(testCase);
            }

            // 8. 更新任务状态
            task.setStatus("SUCCESS");
            task.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(task);

            // 9. 返回生成结果
            TestCaseGenerateResultVO resultVO = new TestCaseGenerateResultVO();
            resultVO.setTaskId(taskId);
            resultVO.setStatus("SUCCESS");
            resultVO.setCaseCount(testCases.size());
            resultVO.setTestCases(testCases.stream().map(this::toVO).toList());
            resultVO.setReferences(ragContext.getReferences());
            resultVO.setRawModelOutput(rawOutput);

            return resultVO;
        } catch (BusinessException e) {
            markTaskFailed(task, e.getMessage());
            throw e;
        } catch (Exception e) {
            markTaskFailed(task, e.getMessage());
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "测试用例生成失败：" + e.getMessage()
            );
        }
    }

    @Override
    public List<TestCaseVO> list(TestCasePageRequest request) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<TestCase>()
                .orderByDesc(TestCase::getCreateTime);

        if (request.getProjectId() != null) {
            wrapper.eq(TestCase::getProjectId, request.getProjectId());
        }

        if (StringUtils.hasText(request.getTaskId())) {
            wrapper.eq(TestCase::getTaskId, request.getTaskId());
        }

        if (StringUtils.hasText(request.getVersionNo())) {
            wrapper.eq(TestCase::getVersionNo, request.getVersionNo());
        }

        if (StringUtils.hasText(request.getModuleCode())) {
            wrapper.eq(TestCase::getModuleCode, request.getModuleCode());
        }

        List<TestCase> testCases = testCaseMapper.selectList(wrapper);

        return testCases.stream().map(this::toVO).toList();
    }

    private TestCaseGenerateTask createTask(String taskId, TestCaseGenerateRequest request) {
        try {
            TestCaseGenerateTask task = new TestCaseGenerateTask();
            task.setTaskId(taskId);
            task.setProjectId(request.getProjectId());
            task.setVersionNo(request.getVersionNo());
            task.setModuleCode(request.getModuleCode());
            task.setGenerateGoal(request.getGenerateGoal());
            task.setGenerateType(
                    StringUtils.hasText(request.getGenerateType())
                            ? request.getGenerateType()
                            : "FULL"
            );
            task.setSelectedSkills(objectMapper.writeValueAsString(request.getSelectedSkills()));
            task.setStatus("RUNNING");
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            taskMapper.insert(task);
            return task;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "创建测试用例生成任务失败：" + e.getMessage()
            );
        }
    }

    private void markTaskFailed(TestCaseGenerateTask task, String errorMessage) {
        task.setStatus("FAILED");
        task.setErrorMessage(errorMessage);
        task.setUpdateTime(LocalDateTime.now());
        taskMapper.updateById(task);
    }

    private List<TestCase> parseAndBuildTestCases(
            String rawOutput,
            String taskId,
            TestCaseGenerateRequest request
    ) {
        try {
            String json = JsonExtractUtils.extractJsonObject(rawOutput);
            JsonNode root = objectMapper.readTree(json);
            JsonNode testCasesNode = root.path("testCases");

            if (!testCasesNode.isArray()) {
                throw new BusinessException(
                        ErrorCode.AI_OUTPUT_PARSE_ERROR,
                        "模型输出中缺少 testCases 数组"
                );
            }

            List<TestCase> result = new ArrayList<>();

            for (JsonNode node : testCasesNode) {
                TestCase testCase = new TestCase();

                // 基础归属信息
                testCase.setTaskId(taskId);
                testCase.setProjectId(request.getProjectId());
                testCase.setVersionNo(request.getVersionNo());
                testCase.setModuleCode(request.getModuleCode());

                // 模型生成内容
                testCase.setModuleName(getText(node, "moduleName"));
                testCase.setCaseTitle(getText(node, "caseTitle"));
                testCase.setCaseType(getText(node, "caseType"));
                testCase.setPriority(getText(node, "priority"));
                testCase.setPrecondition(getText(node, "precondition"));
                testCase.setExpectedResult(getText(node, "expectedResult"));
                testCase.setRiskPoint(getText(node, "riskPoint"));
                testCase.setAutomationSuggestion(getText(node, "automationSuggestion"));

                // JSONB 字段，需要转成 JSON 字符串
                testCase.setSteps(toJsonString(node.path("steps")));
                testCase.setTestData(toJsonString(node.path("testData")));
                testCase.setSourceReferences(toJsonString(node.path("sourceReferences")));

                testCase.setQualityScore(null);
                testCase.setCreateTime(LocalDateTime.now());
                testCase.setUpdateTime(LocalDateTime.now());

                // 默认不是重复用例
                testCase.setDuplicateStatus("NORMAL");
                testCase.setDuplicateOfCaseId(null);
                testCase.setDuplicateScore(null);
                testCase.setDuplicateReason(null);

                // 首次生成的用例来源标记为 AI_GENERATED
                testCase.setSourceType("AI_GENERATED");

                validateTestCase(testCase);

                result.add(testCase);
            }

            return result;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "解析模型测试用例 JSON 失败：" + e.getMessage()
            );
        }
    }

    private void validateTestCase(TestCase testCase) {
        if (!StringUtils.hasText(testCase.getCaseTitle())) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "测试用例标题不能为空"
            );
        }

        if (!StringUtils.hasText(testCase.getExpectedResult())) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "测试用例预期结果不能为空"
            );
        }

        if (!StringUtils.hasText(testCase.getSteps())) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "测试步骤不能为空"
            );
        }
    }

    private String getText(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }

    private String toJsonString(JsonNode node) {
        try {
            if (node == null || node.isMissingNode() || node.isNull()) {
                return "null";
            }
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "JSON 字段转换失败：" + e.getMessage()
            );
        }
    }

    private TestCaseVO toVO(TestCase testCase) {
        TestCaseVO vo = new TestCaseVO();

        vo.setId(testCase.getId());
        vo.setTaskId(testCase.getTaskId());
        vo.setProjectId(testCase.getProjectId());
        vo.setVersionNo(testCase.getVersionNo());
        vo.setModuleCode(testCase.getModuleCode());
        vo.setModuleName(testCase.getModuleName());
        vo.setCaseTitle(testCase.getCaseTitle());
        vo.setCaseType(testCase.getCaseType());
        vo.setPriority(testCase.getPriority());
        vo.setPrecondition(testCase.getPrecondition());
        vo.setSteps(testCase.getSteps());
        vo.setExpectedResult(testCase.getExpectedResult());
        vo.setTestData(testCase.getTestData());
        vo.setSourceReferences(testCase.getSourceReferences());
        vo.setRiskPoint(testCase.getRiskPoint());
        vo.setAutomationSuggestion(testCase.getAutomationSuggestion());
        vo.setQualityScore(testCase.getQualityScore());
        vo.setCreateTime(testCase.getCreateTime());
        vo.setDuplicateStatus(testCase.getDuplicateStatus());
        vo.setDuplicateOfCaseId(testCase.getDuplicateOfCaseId());
        vo.setDuplicateScore(testCase.getDuplicateScore());
        vo.setDuplicateReason(testCase.getDuplicateReason());
        vo.setSourceType(testCase.getSourceType());

        return vo;
    }
}