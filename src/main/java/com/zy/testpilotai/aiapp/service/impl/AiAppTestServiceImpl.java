package com.zy.testpilotai.aiapp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.aiapp.mapper.AiAppTestCaseMapper;
import com.zy.testpilotai.aiapp.mapper.AiAppTestTaskMapper;
import com.zy.testpilotai.aiapp.model.dto.AiAppTestCaseListRequest;
import com.zy.testpilotai.aiapp.model.dto.AiAppTestGenerateRequest;
import com.zy.testpilotai.aiapp.model.entity.AiAppTestCase;
import com.zy.testpilotai.aiapp.model.entity.AiAppTestTask;
import com.zy.testpilotai.aiapp.model.vo.AiAppTestCaseVO;
import com.zy.testpilotai.aiapp.model.vo.AiAppTestGenerateResultVO;
import com.zy.testpilotai.aiapp.model.vo.AiAppTestTaskVO;
import com.zy.testpilotai.aiapp.prompt.AiAppTestPromptBuilder;
import com.zy.testpilotai.aiapp.service.AiAppTestService;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.common.utils.JsonExtractUtils;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.vo.RagContextVO;
import com.zy.testpilotai.knowledge.service.KnowledgeBaseService;
import com.zy.testpilotai.llm.chat.LlmClient;
import com.zy.testpilotai.llm.structured.AiAppTestCaseOutputParser;
import com.zy.testpilotai.llm.structured.dto.AiAppTestCaseItemDTO;
import com.zy.testpilotai.llm.structured.dto.AiAppTestCaseOutputDTO;
import com.zy.testpilotai.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiAppTestServiceImpl implements AiAppTestService {

    private final AiAppTestTaskMapper aiAppTestTaskMapper;

    private final AiAppTestCaseMapper aiAppTestCaseMapper;

    private final ProjectService projectService;

    private final KnowledgeBaseService knowledgeBaseService;

    private final LlmClient llmClient;

    private final AiAppTestPromptBuilder promptBuilder;

    private final ObjectMapper objectMapper;

    private final AiAppTestCaseOutputParser aiAppTestCaseOutputParser;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AiAppTestGenerateResultVO generate(AiAppTestGenerateRequest request) {
        if (request.getProjectId() != null) {
            projectService.getById(request.getProjectId());
        }

        String taskId = "aiapp_" + UUID.randomUUID().toString().replace("-", "");

        AiAppTestTask task = createTask(taskId, request);

        try {
            // 1. 可选构建 RAG 上下文
            RagContextVO ragContext = buildOptionalRagContext(request);

            // 2. 构建 Prompt
            String systemPrompt = promptBuilder.buildSystemPrompt();
            String userPrompt = promptBuilder.buildUserPrompt(request, ragContext);

            // 3. 调用 LLM
            String rawOutput = llmClient.chat(
                    systemPrompt,
                    userPrompt,
                    "AI_APP_TEST_GENERATE",
                    taskId
            );

            // 4. 保存模型原始输出
            task.setRawModelOutput(rawOutput);
            task.setUpdateTime(LocalDateTime.now());
            aiAppTestTaskMapper.updateById(task);

            // 5. 解析模型输出
            List<AiAppTestCase> cases = parseCases(rawOutput, taskId, request);

            if (cases.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.AI_APP_TEST_ERROR,
                        "模型没有生成任何 AI 应用测试用例"
                );
            }

            // 6. 入库测试用例
            for (AiAppTestCase testCase : cases) {
                aiAppTestCaseMapper.insert(testCase);
            }

            // 7. 更新任务状态
            task.setStatus("SUCCESS");
            task.setUpdateTime(LocalDateTime.now());
            aiAppTestTaskMapper.updateById(task);

            // 8. 返回结果
            AiAppTestGenerateResultVO resultVO = new AiAppTestGenerateResultVO();
            resultVO.setTaskId(taskId);
            resultVO.setStatus("SUCCESS");
            resultVO.setCaseCount(cases.size());
            resultVO.setTestCases(cases.stream().map(this::toCaseVO).toList());
            resultVO.setRawModelOutput(rawOutput);

            return resultVO;
        } catch (BusinessException e) {
            markTaskFailed(task, e.getMessage());
            throw e;
        } catch (Exception e) {
            markTaskFailed(task, e.getMessage());
            throw new BusinessException(
                    ErrorCode.AI_APP_TEST_ERROR,
                    "AI 应用测试用例生成失败：" + e.getMessage()
            );
        }
    }

    @Override
    public List<AiAppTestCaseVO> listCases(AiAppTestCaseListRequest request) {
        LambdaQueryWrapper<AiAppTestCase> wrapper =
                new LambdaQueryWrapper<AiAppTestCase>()
                        .orderByDesc(AiAppTestCase::getCreateTime);

        if (StringUtils.hasText(request.getTaskId())) {
            wrapper.eq(AiAppTestCase::getTaskId, request.getTaskId());
        }

        if (StringUtils.hasText(request.getAppType())) {
            wrapper.eq(AiAppTestCase::getAppType, request.getAppType());
        }

        if (StringUtils.hasText(request.getTestDimension())) {
            wrapper.eq(AiAppTestCase::getTestDimension, request.getTestDimension());
        }

        if (StringUtils.hasText(request.getRiskLevel())) {
            wrapper.eq(AiAppTestCase::getRiskLevel, request.getRiskLevel());
        }

        return aiAppTestCaseMapper.selectList(wrapper)
                .stream()
                .map(this::toCaseVO)
                .toList();
    }

    @Override
    public AiAppTestTaskVO getTask(String taskId) {
        AiAppTestTask task = aiAppTestTaskMapper.selectOne(
                new LambdaQueryWrapper<AiAppTestTask>()
                        .eq(AiAppTestTask::getTaskId, taskId)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "AI 应用测试任务不存在");
        }

        return toTaskVO(task);
    }

    private AiAppTestTask createTask(String taskId, AiAppTestGenerateRequest request) {
        try {
            AiAppTestTask task = new AiAppTestTask();
            task.setTaskId(taskId);
            task.setProjectId(request.getProjectId());
            task.setVersionNo(request.getVersionNo());
            task.setModuleCode(request.getModuleCode());
            task.setAppType(request.getAppType());
            task.setAppDescription(request.getAppDescription());
            task.setGenerateGoal(request.getGenerateGoal());
            task.setTestDimensions(objectMapper.writeValueAsString(request.getTestDimensions()));
            task.setSelectedSkills(objectMapper.writeValueAsString(request.getSelectedSkills()));
            task.setStatus("RUNNING");
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            aiAppTestTaskMapper.insert(task);
            return task;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_APP_TEST_ERROR,
                    "创建 AI 应用测试任务失败：" + e.getMessage()
            );
        }
    }

    private RagContextVO buildOptionalRagContext(AiAppTestGenerateRequest request) {
        /*
         * 如果没有 projectId，就不走知识库。
         */
        if (request.getProjectId() == null) {
            return null;
        }

        try {
            KnowledgeSearchRequest searchRequest = new KnowledgeSearchRequest();
            searchRequest.setProjectId(request.getProjectId());
            searchRequest.setVersionNo(request.getVersionNo());
            searchRequest.setModuleCode(request.getModuleCode());
            searchRequest.setTopK(request.getTopK());
            searchRequest.setQuery(
                    StringUtils.hasText(request.getGenerateGoal())
                            ? request.getGenerateGoal()
                            : request.getAppDescription()
            );

            return knowledgeBaseService.buildRagContext(searchRequest);
        } catch (Exception e) {
            return null;
        }
    }

    private List<AiAppTestCase> parseCases(
            String rawOutput,
            String taskId,
            AiAppTestGenerateRequest request
    ) {
        AiAppTestCaseOutputDTO output = aiAppTestCaseOutputParser.parse(rawOutput);

        List<AiAppTestCase> result = new ArrayList<>();

        for (AiAppTestCaseItemDTO item : output.getTestCases()) {
            AiAppTestCase testCase = new AiAppTestCase();

            testCase.setTaskId(taskId);

            testCase.setAppType(
                    StringUtils.hasText(item.getAppType())
                            ? item.getAppType()
                            : request.getAppType()
            );

            testCase.setTestDimension(item.getTestDimension());
            testCase.setCaseTitle(item.getCaseTitle());
            testCase.setPriority(item.getPriority());
            testCase.setAttackPrompt(item.getAttackPrompt());
            testCase.setInputData(item.getInputData());
            testCase.setPrecondition(item.getPrecondition());
            testCase.setSteps(item.getSteps());
            testCase.setExpectedBehavior(item.getExpectedBehavior());
            testCase.setPassCriteria(item.getPassCriteria());
            testCase.setEvaluationMethod(item.getEvaluationMethod());
            testCase.setRiskLevel(item.getRiskLevel());
            testCase.setAutomationSuggestion(item.getAutomationSuggestion());
            testCase.setSourceReferences(item.getSourceReferences());

            testCase.setCreateTime(LocalDateTime.now());
            testCase.setUpdateTime(LocalDateTime.now());

            result.add(testCase);
        }

        return result;
    }

    private void markTaskFailed(AiAppTestTask task, String errorMessage) {
        task.setStatus("FAILED");
        task.setErrorMessage(errorMessage);
        task.setUpdateTime(LocalDateTime.now());
        aiAppTestTaskMapper.updateById(task);
    }

    private AiAppTestCaseVO toCaseVO(AiAppTestCase testCase) {
        AiAppTestCaseVO vo = new AiAppTestCaseVO();

        vo.setId(testCase.getId());
        vo.setTaskId(testCase.getTaskId());
        vo.setAppType(testCase.getAppType());
        vo.setTestDimension(testCase.getTestDimension());
        vo.setCaseTitle(testCase.getCaseTitle());
        vo.setPriority(testCase.getPriority());
        vo.setAttackPrompt(testCase.getAttackPrompt());
        vo.setInputData(testCase.getInputData());
        vo.setPrecondition(testCase.getPrecondition());
        vo.setSteps(testCase.getSteps());
        vo.setExpectedBehavior(testCase.getExpectedBehavior());
        vo.setPassCriteria(testCase.getPassCriteria());
        vo.setEvaluationMethod(testCase.getEvaluationMethod());
        vo.setRiskLevel(testCase.getRiskLevel());
        vo.setAutomationSuggestion(testCase.getAutomationSuggestion());
        vo.setSourceReferences(testCase.getSourceReferences());
        vo.setCreateTime(testCase.getCreateTime());

        return vo;
    }

    private AiAppTestTaskVO toTaskVO(AiAppTestTask task) {
        AiAppTestTaskVO vo = new AiAppTestTaskVO();

        vo.setTaskId(task.getTaskId());
        vo.setProjectId(task.getProjectId());
        vo.setVersionNo(task.getVersionNo());
        vo.setModuleCode(task.getModuleCode());
        vo.setAppType(task.getAppType());
        vo.setAppDescription(task.getAppDescription());
        vo.setGenerateGoal(task.getGenerateGoal());
        vo.setTestDimensions(task.getTestDimensions());
        vo.setSelectedSkills(task.getSelectedSkills());
        vo.setStatus(task.getStatus());
        vo.setRawModelOutput(task.getRawModelOutput());
        vo.setErrorMessage(task.getErrorMessage());
        vo.setCreateTime(task.getCreateTime());
        vo.setUpdateTime(task.getUpdateTime());

        return vo;
    }
}