package com.zy.testpilotai.testcase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.vo.RagContextVO;
import com.zy.testpilotai.knowledge.service.KnowledgeBaseService;
import com.zy.testpilotai.llm.chat.LlmClient;
import com.zy.testpilotai.llm.structured.AiTestCaseOutputParser;
import com.zy.testpilotai.llm.structured.AiTestCaseReviewOutputParser;
import com.zy.testpilotai.llm.structured.dto.AiGeneratedTestCaseItemDTO;
import com.zy.testpilotai.llm.structured.dto.AiGeneratedTestCaseOutputDTO;
import com.zy.testpilotai.llm.structured.dto.AiTestCaseReviewOutputDTO;
import com.zy.testpilotai.testcase.mapper.TestCaseGenerateTaskMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseQualityReviewTaskMapper;
import com.zy.testpilotai.testcase.model.dto.MissingCaseCompleteRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseReviewRequest;
import com.zy.testpilotai.testcase.model.entity.TestCase;
import com.zy.testpilotai.testcase.model.entity.TestCaseGenerateTask;
import com.zy.testpilotai.testcase.model.entity.TestCaseQualityReviewTask;
import com.zy.testpilotai.testcase.model.vo.MissingCaseCompleteResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseQualityReviewResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseVO;
import com.zy.testpilotai.testcase.prompt.TestCaseReviewPromptBuilder;
import com.zy.testpilotai.testcase.service.TestCaseQualityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestCaseQualityServiceImpl implements TestCaseQualityService {

    private final TestCaseGenerateTaskMapper taskMapper;

    private final TestCaseMapper testCaseMapper;

    private final TestCaseQualityReviewTaskMapper reviewTaskMapper;

    private final KnowledgeBaseService knowledgeBaseService;

    private final LlmClient llmClient;

    private final TestCaseReviewPromptBuilder promptBuilder;

    private final ObjectMapper objectMapper;

    private final AiTestCaseOutputParser aiTestCaseOutputParser;

    private final AiTestCaseReviewOutputParser aiTestCaseReviewOutputParser;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseQualityReviewResultVO review(TestCaseReviewRequest request) {
        // 1. 查询原始生成任务
        TestCaseGenerateTask sourceTask = getSourceTask(request.getTaskId());

        // 2. 查询该任务下的测试用例
        List<TestCase> testCases = listCasesByTaskId(request.getTaskId());

        if (testCases.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.TESTCASE_REVIEW_ERROR,
                    "该任务下没有测试用例，无法进行质量评审"
            );
        }

        String reviewTaskId = "rv_" + UUID.randomUUID().toString().replace("-", "");

        TestCaseQualityReviewTask reviewTask = createReviewTask(reviewTaskId, sourceTask);

        try {
            // 3. 构建评审 Prompt
            String systemPrompt = promptBuilder.buildReviewSystemPrompt();
            String userPrompt = promptBuilder.buildReviewUserPrompt(sourceTask, testCases);

            // 4. 调用 LLM 做质量评审
            String rawOutput = llmClient.chat(
                    systemPrompt,
                    userPrompt,
                    "TESTCASE_REVIEW",
                    reviewTaskId
            );

            AiTestCaseReviewOutputDTO reviewOutput =
                    aiTestCaseReviewOutputParser.parse(rawOutput);

            Double totalScore = reviewOutput.getTotalScore() == null
                    ? 0.0
                    : reviewOutput.getTotalScore().doubleValue();

            String missingPoints = reviewOutput.getMissingPoints();

            String suggestedCaseDirections = missingPoints;

            String reviewResultJson = buildReviewResultJson(reviewOutput);

            // 6. 更新评审任务
            reviewTask.setStatus("SUCCESS");
            reviewTask.setTotalScore(totalScore);
            reviewTask.setReviewResult(reviewResultJson);
            reviewTask.setMissingPoints(missingPoints);
            reviewTask.setSuggestedCaseDirections(suggestedCaseDirections);
            reviewTask.setRawModelOutput(rawOutput);
            reviewTask.setUpdateTime(LocalDateTime.now());
            reviewTaskMapper.updateById(reviewTask);

            // 7. 更新生成任务总评分
            sourceTask.setQualityScore(totalScore);
            sourceTask.setUpdateTime(LocalDateTime.now());
            taskMapper.updateById(sourceTask);

            // 8. 更新该任务下所有测试用例的质量评分
            for (TestCase testCase : testCases) {
                testCase.setQualityScore(totalScore);
                testCase.setUpdateTime(LocalDateTime.now());
                testCaseMapper.updateById(testCase);
            }

            return toReviewVO(reviewTask);
        } catch (BusinessException e) {
            markReviewFailed(reviewTask, e.getMessage());
            throw e;
        } catch (Exception e) {
            markReviewFailed(reviewTask, e.getMessage());
            throw new BusinessException(
                    ErrorCode.TESTCASE_REVIEW_ERROR,
                    "测试用例质量评审失败：" + e.getMessage()
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MissingCaseCompleteResultVO completeMissing(MissingCaseCompleteRequest request) {
        // 1. 查询原始生成任务
        TestCaseGenerateTask sourceTask = getSourceTask(request.getTaskId());

        // 2. 查询评审任务
        TestCaseQualityReviewTask reviewTask = getReviewTask(
                request.getTaskId(),
                request.getReviewTaskId()
        );

        if (!StringUtils.hasText(reviewTask.getMissingPoints())
                || "[]".equals(reviewTask.getMissingPoints())) {
            throw new BusinessException(
                    ErrorCode.TESTCASE_COMPLETE_ERROR,
                    "当前评审结果没有缺失测试点，无需补全"
            );
        }

        // 3. 查询已有用例，补全时避免重复
        List<TestCase> existingCases = listCasesByTaskId(request.getTaskId());

        // 4. 根据原始任务和缺失点重新构建 RAG 查询
        KnowledgeSearchRequest searchRequest = new KnowledgeSearchRequest();
        searchRequest.setProjectId(sourceTask.getProjectId());
        searchRequest.setVersionNo(sourceTask.getVersionNo());
        searchRequest.setModuleCode(sourceTask.getModuleCode());
        searchRequest.setTopK(request.getTopK());
        searchRequest.setQuery(
                sourceTask.getGenerateGoal()
                        + "\n缺失测试点："
                        + reviewTask.getMissingPoints()
        );

        RagContextVO ragContext = knowledgeBaseService.buildRagContext(searchRequest);

        // 5. 构建补全 Prompt
        String systemPrompt = promptBuilder.buildCompleteSystemPrompt();
        String userPrompt = promptBuilder.buildCompleteUserPrompt(
                sourceTask,
                existingCases,
                reviewTask.getMissingPoints(),
                ragContext
        );

        try {
            // 6. 调用 LLM 补全缺失用例
            String rawOutput = llmClient.chat(
                    systemPrompt,
                    userPrompt,
                    "TESTCASE_COMPLETE_MISSING",
                    sourceTask.getTaskId()
            );

            // 7. 解析补全用例
            List<TestCase> addedCases = parseAndBuildTestCases(
                    rawOutput,
                    sourceTask.getTaskId(),
                    sourceTask
            );

            if (addedCases.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.TESTCASE_COMPLETE_ERROR,
                        "模型没有补全任何测试用例"
                );
            }

            // 8. 入库补全用例
            for (TestCase testCase : addedCases) {
                testCaseMapper.insert(testCase);
            }

            MissingCaseCompleteResultVO resultVO = new MissingCaseCompleteResultVO();
            resultVO.setTaskId(sourceTask.getTaskId());
            resultVO.setReviewTaskId(reviewTask.getReviewTaskId());
            resultVO.setAddedCaseCount(addedCases.size());
            resultVO.setAddedCases(addedCases.stream().map(this::toVO).toList());
            resultVO.setRawModelOutput(rawOutput);

            return resultVO;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.TESTCASE_COMPLETE_ERROR,
                    "缺失测试用例补全失败：" + e.getMessage()
            );
        }
    }

    private TestCaseGenerateTask getSourceTask(String taskId) {
        TestCaseGenerateTask task = taskMapper.selectOne(
                new LambdaQueryWrapper<TestCaseGenerateTask>()
                        .eq(TestCaseGenerateTask::getTaskId, taskId)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "测试用例生成任务不存在"
            );
        }

        return task;
    }

    private List<TestCase> listCasesByTaskId(String taskId) {
        return testCaseMapper.selectList(
                new LambdaQueryWrapper<TestCase>()
                        .eq(TestCase::getTaskId, taskId)
                        .orderByAsc(TestCase::getId)
        );
    }

    private TestCaseQualityReviewTask createReviewTask(
            String reviewTaskId,
            TestCaseGenerateTask sourceTask
    ) {
        TestCaseQualityReviewTask reviewTask = new TestCaseQualityReviewTask();

        reviewTask.setReviewTaskId(reviewTaskId);
        reviewTask.setSourceTaskId(sourceTask.getTaskId());
        reviewTask.setProjectId(sourceTask.getProjectId());
        reviewTask.setVersionNo(sourceTask.getVersionNo());
        reviewTask.setModuleCode(sourceTask.getModuleCode());
        reviewTask.setStatus("RUNNING");
        reviewTask.setCreateTime(LocalDateTime.now());
        reviewTask.setUpdateTime(LocalDateTime.now());

        reviewTaskMapper.insert(reviewTask);

        return reviewTask;
    }

    private TestCaseQualityReviewTask getReviewTask(
            String sourceTaskId,
            String reviewTaskId
    ) {
        LambdaQueryWrapper<TestCaseQualityReviewTask> wrapper =
                new LambdaQueryWrapper<TestCaseQualityReviewTask>()
                        .eq(TestCaseQualityReviewTask::getSourceTaskId, sourceTaskId)
                        .eq(TestCaseQualityReviewTask::getStatus, "SUCCESS")
                        .orderByDesc(TestCaseQualityReviewTask::getCreateTime);

        if (StringUtils.hasText(reviewTaskId)) {
            wrapper.eq(TestCaseQualityReviewTask::getReviewTaskId, reviewTaskId);
        }

        TestCaseQualityReviewTask reviewTask = reviewTaskMapper.selectOne(
                wrapper.last("LIMIT 1")
        );

        if (reviewTask == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "未找到成功的质量评审记录，请先执行质量评审"
            );
        }

        return reviewTask;
    }

    private void markReviewFailed(
            TestCaseQualityReviewTask reviewTask,
            String errorMessage
    ) {
        reviewTask.setStatus("FAILED");
        reviewTask.setErrorMessage(errorMessage);
        reviewTask.setUpdateTime(LocalDateTime.now());
        reviewTaskMapper.updateById(reviewTask);
    }

    /**
     * 把结构化质量评审结果组装成标准 JSON。
     */
    private String buildReviewResultJson(AiTestCaseReviewOutputDTO reviewOutput) {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("totalScore", reviewOutput.getTotalScore());
        result.put("dimensions", readJsonOrRaw(reviewOutput.getDimensions()));
        result.put("missingPoints", readJsonOrRaw(reviewOutput.getMissingPoints()));
        result.put("duplicateCases", readJsonOrRaw(reviewOutput.getDuplicateCases()));
        result.put("lowQualityCases", readJsonOrRaw(reviewOutput.getLowQualityCases()));
        result.put("summary", reviewOutput.getSummary());

        return toJsonString(result);
    }

    /**
     * 尝试把 JSON 字符串转成 JsonNode。
     */
    private Object readJsonOrRaw(String json) {
        if (!StringUtils.hasText(json)) {
            return null;
        }

        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            return json;
        }
    }

    private List<TestCase> parseAndBuildTestCases(
            String rawOutput,
            String taskId,
            TestCaseGenerateTask sourceTask
    ) {
        AiGeneratedTestCaseOutputDTO output = aiTestCaseOutputParser.parse(rawOutput);

        List<TestCase> result = new ArrayList<>();

        for (AiGeneratedTestCaseItemDTO item : output.getTestCases()) {
            TestCase testCase = new TestCase();

            testCase.setTaskId(taskId);
            testCase.setProjectId(sourceTask.getProjectId());
            testCase.setVersionNo(sourceTask.getVersionNo());

            testCase.setModuleCode(
                    StringUtils.hasText(item.getModuleCode())
                            ? item.getModuleCode()
                            : sourceTask.getModuleCode()
            );

            testCase.setModuleName(item.getModuleName());
            testCase.setCaseTitle(item.getCaseTitle());
            testCase.setCaseType(item.getCaseType());
            testCase.setPriority(item.getPriority());
            testCase.setPrecondition(item.getPrecondition());
            testCase.setSteps(item.getSteps());
            testCase.setExpectedResult(item.getExpectedResult());
            testCase.setTestData(item.getTestData());
            testCase.setSourceReferences(item.getSourceReferences());
            testCase.setRiskPoint(item.getRiskPoint());
            testCase.setAutomationSuggestion(item.getAutomationSuggestion());

            testCase.setQualityScore(null);

            // 补全出来的用例默认不是重复用例
            testCase.setDuplicateStatus("NORMAL");
            testCase.setDuplicateOfCaseId(null);
            testCase.setDuplicateScore(null);
            testCase.setDuplicateReason(null);

            // 来源标记为 AI 补全
            testCase.setSourceType("AI_COMPLETED");

            testCase.setCreateTime(LocalDateTime.now());
            testCase.setUpdateTime(LocalDateTime.now());

            result.add(testCase);
        }

        return result;
    }

    private String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "JSON 字段转换失败：" + e.getMessage()
            );
        }
    }

    private TestCaseQualityReviewResultVO toReviewVO(
            TestCaseQualityReviewTask reviewTask
    ) {
        TestCaseQualityReviewResultVO vo = new TestCaseQualityReviewResultVO();

        vo.setReviewTaskId(reviewTask.getReviewTaskId());
        vo.setSourceTaskId(reviewTask.getSourceTaskId());
        vo.setStatus(reviewTask.getStatus());
        vo.setTotalScore(reviewTask.getTotalScore());
        vo.setReviewResult(reviewTask.getReviewResult());
        vo.setMissingPoints(reviewTask.getMissingPoints());
        vo.setSuggestedCaseDirections(reviewTask.getSuggestedCaseDirections());
        vo.setRawModelOutput(reviewTask.getRawModelOutput());
        vo.setErrorMessage(reviewTask.getErrorMessage());

        return vo;
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