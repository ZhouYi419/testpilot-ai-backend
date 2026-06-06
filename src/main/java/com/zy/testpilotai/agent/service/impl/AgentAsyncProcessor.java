package com.zy.testpilotai.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.agent.config.AgentExecutionProperties;
import com.zy.testpilotai.agent.mapper.AgentTaskMapper;
import com.zy.testpilotai.agent.mapper.AgentTaskStepMapper;
import com.zy.testpilotai.agent.model.entity.AgentTask;
import com.zy.testpilotai.agent.model.entity.AgentTaskStep;
import com.zy.testpilotai.agent.service.AgentExecutionLogService;
import com.zy.testpilotai.aiapp.model.dto.AiAppTestGenerateRequest;
import com.zy.testpilotai.aiapp.model.vo.AiAppTestGenerateResultVO;
import com.zy.testpilotai.aiapp.service.AiAppTestService;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.requirement.model.dto.ChangeImpactAnalyzeRequest;
import com.zy.testpilotai.requirement.model.dto.IncrementalTestCaseGenerateRequest;
import com.zy.testpilotai.requirement.model.vo.ChangeImpactAnalyzeResultVO;
import com.zy.testpilotai.requirement.model.vo.IncrementalTestCaseGenerateResultVO;
import com.zy.testpilotai.requirement.service.RequirementChangeService;
import com.zy.testpilotai.testcase.model.dto.MissingCaseCompleteRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseDeduplicateRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseGenerateRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseReviewRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseDeduplicateResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseGenerateResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseQualityReviewResultVO;
import com.zy.testpilotai.testcase.service.TestCaseGenerateService;
import com.zy.testpilotai.testcase.service.TestCaseQualityService;
import com.zy.testpilotai.testcase.service.TestCaseToolService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AgentAsyncProcessor {

    private final AgentTaskMapper agentTaskMapper;

    private final AgentTaskStepMapper agentTaskStepMapper;

    private final TestCaseGenerateService testCaseGenerateService;

    private final TestCaseQualityService testCaseQualityService;

    private final TestCaseToolService testCaseToolService;

    private final RequirementChangeService requirementChangeService;

    private final AiAppTestService aiAppTestService;

    private final ObjectMapper objectMapper;

    private final AgentExecutionLogService agentExecutionLogService;

    private final AgentExecutionProperties agentExecutionProperties;

    /**
     * 异步执行 Agent。
     */
    @Async("agentTaskExecutor")
    public void execute(String agentTaskId) {
        AgentTask agentTask = getAgentTask(agentTaskId);

        long startMillis = System.currentTimeMillis();

        agentExecutionLogService.info(
                agentTaskId,
                null,
                null,
                null,
                "AGENT_START",
                "Agent 任务开始执行，workflowType=" + agentTask.getWorkflowType(),
                null,
                null,
                null
        );

        try {
            markAgentRunning(agentTask);

            checkTaskTimeout(agentTaskId);

            switch (agentTask.getWorkflowType()) {
                case "STANDARD_TEST_DESIGN" -> runStandardTestDesign(agentTask);
                case "INCREMENTAL_TEST_DESIGN" -> runIncrementalTestDesign(agentTask);
                case "AI_APP_TEST_DESIGN" -> runAiAppTestDesign(agentTask);
                default -> throw new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "不支持的 Agent 工作流类型：" + agentTask.getWorkflowType()
                );
            }

            checkCancel(agentTaskId);
            checkTaskTimeout(agentTaskId);

            agentTask = getAgentTask(agentTaskId);
            agentTask.setStatus("SUCCESS");
            agentTask.setEndTime(LocalDateTime.now());
            agentTask.setUpdateTime(LocalDateTime.now());
            agentTaskMapper.updateById(agentTask);

            agentExecutionLogService.info(
                    agentTaskId,
                    null,
                    null,
                    null,
                    "AGENT_SUCCESS",
                    "Agent 任务执行成功",
                    null,
                    agentTask.getFinalResult(),
                    System.currentTimeMillis() - startMillis
            );
        } catch (BusinessException e) {
            markAgentFailed(agentTaskId, e.getMessage());

            agentExecutionLogService.error(
                    agentTaskId,
                    null,
                    null,
                    null,
                    "AGENT_FAILED",
                    "Agent 任务执行失败：" + e.getMessage(),
                    null,
                    e,
                    System.currentTimeMillis() - startMillis
            );
        } catch (Exception e) {
            markAgentFailed(agentTaskId, "Agent 执行失败：" + e.getMessage());

            agentExecutionLogService.error(
                    agentTaskId,
                    null,
                    null,
                    null,
                    "AGENT_FAILED",
                    "Agent 任务执行异常：" + e.getMessage(),
                    null,
                    e,
                    System.currentTimeMillis() - startMillis
            );
        }
    }

    /**
     * 普通测试设计 Agent。
     *
     * 流程：
     * 1. 生成测试用例
     * 2. 质量评审，可通过 autoReview 控制
     * 3. 补全缺失用例，可通过 autoCompleteMissing 控制
     * 4. 测试用例去重，可通过 autoDeduplicate 控制
     */
    private void runStandardTestDesign(AgentTask agentTask) {
        int resumeFromStep = normalizeResumeStep(agentTask.getResumeFromStep());

        final String agentTaskId = agentTask.getAgentTaskId();
        final Long projectId = agentTask.getProjectId();
        final String targetVersionNo = agentTask.getTargetVersionNo();
        final String moduleCode = agentTask.getModuleCode();
        final String userGoal = agentTask.getUserGoal();
        final String selectedSkillsJson = agentTask.getSelectedSkills();

        TestCaseGenerateResultVO generateResult;

        if (resumeFromStep <= 1) {
            generateResult = executeStep(
                    agentTaskId,
                    1,
                    "GENERATE",
                    "生成测试用例",
                    Map.of("userGoal", userGoal),
                    () -> {
                        checkCancel(agentTaskId);

                        AgentTask latestTask = getAgentTask(agentTaskId);

                        TestCaseGenerateRequest request = new TestCaseGenerateRequest();
                        request.setProjectId(projectId);
                        request.setVersionNo(targetVersionNo);
                        request.setModuleCode(moduleCode);
                        request.setGenerateGoal(userGoal);
                        request.setGenerateType("AGENT_STANDARD");
                        request.setSelectedSkills(parseSkills(selectedSkillsJson));
                        request.setTopK(safeTopK(latestTask));

                        return testCaseGenerateService.generate(request);
                    }
            );

            AgentTask latestTask = getAgentTask(agentTaskId);
            latestTask.setTestcaseTaskId(generateResult.getTaskId());
            latestTask.setUpdateTime(LocalDateTime.now());
            agentTaskMapper.updateById(latestTask);
        }

        String testcaseTaskId = getAgentTask(agentTaskId).getTestcaseTaskId();

        if (!StringUtils.hasText(testcaseTaskId)) {
            throw new BusinessException(
                    ErrorCode.AGENT_EXECUTION_ERROR,
                    "缺少测试用例生成任务ID，无法继续执行"
            );
        }

        TestCaseQualityReviewResultVO reviewResult = null;

        AgentTask latestForReview = getAgentTask(agentTaskId);

        if (resumeFromStep <= 2 && enabled(latestForReview.getAutoReview())) {
            reviewResult = executeStep(
                    agentTaskId,
                    2,
                    "REVIEW",
                    "质量评审",
                    Map.of("taskId", testcaseTaskId),
                    () -> {
                        checkCancel(agentTaskId);

                        TestCaseReviewRequest request = new TestCaseReviewRequest();
                        request.setTaskId(testcaseTaskId);

                        return testCaseQualityService.review(request);
                    }
            );
        } else if (resumeFromStep <= 2) {
            createSkippedStep(
                    agentTaskId,
                    2,
                    "REVIEW",
                    "质量评审",
                    "autoReview=false，跳过质量评审"
            );
        }

        AgentTask latestForComplete = getAgentTask(agentTaskId);

        if (resumeFromStep <= 3 && enabled(latestForComplete.getAutoCompleteMissing())) {
            String reviewTaskId = reviewResult == null ? null : reviewResult.getReviewTaskId();

            if (StringUtils.hasText(reviewTaskId)) {
                executeStep(
                        agentTaskId,
                        3,
                        "COMPLETE",
                        "补全缺失用例",
                        Map.of("taskId", testcaseTaskId, "reviewTaskId", reviewTaskId),
                        () -> {
                            checkCancel(agentTaskId);

                            AgentTask latestTask = getAgentTask(agentTaskId);

                            MissingCaseCompleteRequest request = new MissingCaseCompleteRequest();
                            request.setTaskId(testcaseTaskId);
                            request.setReviewTaskId(reviewTaskId);
                            request.setTopK(safeTopK(latestTask));

                            return testCaseQualityService.completeMissing(request);
                        }
                );
            } else {
                createSkippedStep(
                        agentTaskId,
                        3,
                        "COMPLETE",
                        "补全缺失用例",
                        "没有本次评审结果，跳过补全"
                );
            }
        } else if (resumeFromStep <= 3) {
            createSkippedStep(
                    agentTaskId,
                    3,
                    "COMPLETE",
                    "补全缺失用例",
                    "autoCompleteMissing=false，跳过补全"
            );
        }

        TestCaseDeduplicateResultVO deduplicateResult = null;

        AgentTask latestForDeduplicate = getAgentTask(agentTaskId);

        if (resumeFromStep <= 4 && enabled(latestForDeduplicate.getAutoDeduplicate())) {
            deduplicateResult = executeStep(
                    agentTaskId,
                    4,
                    "DEDUPLICATE",
                    "测试用例去重",
                    Map.of("taskId", testcaseTaskId),
                    () -> {
                        checkCancel(agentTaskId);

                        AgentTask latestTask = getAgentTask(agentTaskId);

                        TestCaseDeduplicateRequest request = new TestCaseDeduplicateRequest();
                        request.setTaskId(testcaseTaskId);
                        request.setThreshold(safeDeduplicateThreshold(latestTask));

                        return testCaseToolService.deduplicate(request);
                    }
            );
        } else if (resumeFromStep <= 4) {
            createSkippedStep(
                    agentTaskId,
                    4,
                    "DEDUPLICATE",
                    "测试用例去重",
                    "autoDeduplicate=false，跳过去重"
            );
        }

        Map<String, Object> finalResult = new LinkedHashMap<>();
        finalResult.put("workflowType", "STANDARD_TEST_DESIGN");
        finalResult.put("testcaseTaskId", testcaseTaskId);
        finalResult.put("duplicateCaseCount", deduplicateResult == null ? null : deduplicateResult.getDuplicateCaseCount());
        finalResult.put("exportApi", "/api/testcase/export");
        finalResult.put("exportCondition", Map.of(
                "taskId",
                testcaseTaskId,
                "includeDuplicate",
                false
        ));

        AgentTask latest = getAgentTask(agentTaskId);
        latest.setFinalResult(toJson(finalResult));
        latest.setUpdateTime(LocalDateTime.now());
        agentTaskMapper.updateById(latest);
    }

    /**
     * 新需求增量测试 Agent。
     *
     * 流程：
     * 1. 新需求影响分析
     * 2. 生成增量测试用例
     * 3. 质量评审，可通过 autoReview 控制
     * 4. 补全缺失用例，可通过 autoCompleteMissing 控制
     * 5. 测试用例去重，可通过 autoDeduplicate 控制
     */
    private void runIncrementalTestDesign(AgentTask agentTask) {
        int resumeFromStep = normalizeResumeStep(agentTask.getResumeFromStep());

        final String agentTaskId = agentTask.getAgentTaskId();
        final Long projectId = agentTask.getProjectId();
        final String baseVersionNo = agentTask.getBaseVersionNo();
        final String targetVersionNo = agentTask.getTargetVersionNo();
        final String newRequirement = agentTask.getNewRequirement();
        final String selectedSkillsJson = agentTask.getSelectedSkills();

        ChangeImpactAnalyzeResultVO impactResult;

        if (resumeFromStep <= 1) {
            impactResult = executeStep(
                    agentTaskId,
                    1,
                    "IMPACT_ANALYZE",
                    "新需求影响分析",
                    Map.of("newRequirement", newRequirement),
                    () -> {
                        checkCancel(agentTaskId);

                        AgentTask latestTask = getAgentTask(agentTaskId);

                        ChangeImpactAnalyzeRequest request = new ChangeImpactAnalyzeRequest();
                        request.setProjectId(projectId);
                        request.setBaseVersionNo(baseVersionNo);
                        request.setTargetVersionNo(targetVersionNo);
                        request.setNewRequirement(newRequirement);
                        request.setTopK(safeTopK(latestTask));

                        return requirementChangeService.analyzeImpact(request);
                    }
            );

            AgentTask latestTask = getAgentTask(agentTaskId);
            latestTask.setAnalysisTaskId(impactResult.getAnalysisTaskId());
            latestTask.setUpdateTime(LocalDateTime.now());
            agentTaskMapper.updateById(latestTask);
        }

        String analysisTaskId = getAgentTask(agentTaskId).getAnalysisTaskId();

        if (!StringUtils.hasText(analysisTaskId)) {
            throw new BusinessException(
                    ErrorCode.AGENT_EXECUTION_ERROR,
                    "缺少影响分析任务ID，无法继续执行"
            );
        }

        IncrementalTestCaseGenerateResultVO incrementalResult;

        if (resumeFromStep <= 2) {
            incrementalResult = executeStep(
                    agentTaskId,
                    2,
                    "GENERATE_INCREMENTAL",
                    "生成增量测试用例",
                    Map.of("analysisTaskId", analysisTaskId),
                    () -> {
                        checkCancel(agentTaskId);

                        AgentTask latestTask = getAgentTask(agentTaskId);

                        IncrementalTestCaseGenerateRequest request = new IncrementalTestCaseGenerateRequest();
                        request.setAnalysisTaskId(analysisTaskId);
                        request.setSelectedSkills(parseSkills(selectedSkillsJson));
                        request.setTopK(safeTopK(latestTask));

                        return requirementChangeService.generateIncrementalCases(request);
                    }
            );

            AgentTask latestTask = getAgentTask(agentTaskId);
            latestTask.setTestcaseTaskId(incrementalResult.getTaskId());
            latestTask.setUpdateTime(LocalDateTime.now());
            agentTaskMapper.updateById(latestTask);
        }

        String testcaseTaskId = getAgentTask(agentTaskId).getTestcaseTaskId();

        if (!StringUtils.hasText(testcaseTaskId)) {
            throw new BusinessException(
                    ErrorCode.AGENT_EXECUTION_ERROR,
                    "缺少测试用例任务ID，无法继续执行"
            );
        }

        TestCaseQualityReviewResultVO reviewResult = null;

        AgentTask latestForReview = getAgentTask(agentTaskId);

        if (resumeFromStep <= 3 && enabled(latestForReview.getAutoReview())) {
            reviewResult = executeStep(
                    agentTaskId,
                    3,
                    "REVIEW",
                    "质量评审",
                    Map.of("taskId", testcaseTaskId),
                    () -> {
                        checkCancel(agentTaskId);

                        TestCaseReviewRequest request = new TestCaseReviewRequest();
                        request.setTaskId(testcaseTaskId);

                        return testCaseQualityService.review(request);
                    }
            );
        } else if (resumeFromStep <= 3) {
            createSkippedStep(
                    agentTaskId,
                    3,
                    "REVIEW",
                    "质量评审",
                    "autoReview=false，跳过质量评审"
            );
        }

        AgentTask latestForComplete = getAgentTask(agentTaskId);

        if (resumeFromStep <= 4 && enabled(latestForComplete.getAutoCompleteMissing())) {
            String reviewTaskId = reviewResult == null ? null : reviewResult.getReviewTaskId();

            if (StringUtils.hasText(reviewTaskId)) {
                executeStep(
                        agentTaskId,
                        4,
                        "COMPLETE",
                        "补全缺失用例",
                        Map.of("taskId", testcaseTaskId, "reviewTaskId", reviewTaskId),
                        () -> {
                            checkCancel(agentTaskId);

                            AgentTask latestTask = getAgentTask(agentTaskId);

                            MissingCaseCompleteRequest request = new MissingCaseCompleteRequest();
                            request.setTaskId(testcaseTaskId);
                            request.setReviewTaskId(reviewTaskId);
                            request.setTopK(safeTopK(latestTask));

                            return testCaseQualityService.completeMissing(request);
                        }
                );
            } else {
                createSkippedStep(
                        agentTaskId,
                        4,
                        "COMPLETE",
                        "补全缺失用例",
                        "没有本次评审结果，跳过补全"
                );
            }
        } else if (resumeFromStep <= 4) {
            createSkippedStep(
                    agentTaskId,
                    4,
                    "COMPLETE",
                    "补全缺失用例",
                    "autoCompleteMissing=false，跳过补全"
            );
        }

        TestCaseDeduplicateResultVO deduplicateResult = null;

        AgentTask latestForDeduplicate = getAgentTask(agentTaskId);

        if (resumeFromStep <= 5 && enabled(latestForDeduplicate.getAutoDeduplicate())) {
            deduplicateResult = executeStep(
                    agentTaskId,
                    5,
                    "DEDUPLICATE",
                    "测试用例去重",
                    Map.of("taskId", testcaseTaskId),
                    () -> {
                        checkCancel(agentTaskId);

                        AgentTask latestTask = getAgentTask(agentTaskId);

                        TestCaseDeduplicateRequest request = new TestCaseDeduplicateRequest();
                        request.setTaskId(testcaseTaskId);
                        request.setThreshold(safeDeduplicateThreshold(latestTask));

                        return testCaseToolService.deduplicate(request);
                    }
            );
        } else if (resumeFromStep <= 5) {
            createSkippedStep(
                    agentTaskId,
                    5,
                    "DEDUPLICATE",
                    "测试用例去重",
                    "autoDeduplicate=false，跳过去重"
            );
        }

        Map<String, Object> finalResult = new LinkedHashMap<>();
        finalResult.put("workflowType", "INCREMENTAL_TEST_DESIGN");
        finalResult.put("analysisTaskId", analysisTaskId);
        finalResult.put("testcaseTaskId", testcaseTaskId);
        finalResult.put("baseVersionNo", baseVersionNo);
        finalResult.put("targetVersionNo", targetVersionNo);
        finalResult.put("duplicateCaseCount", deduplicateResult == null ? null : deduplicateResult.getDuplicateCaseCount());
        finalResult.put("exportApi", "/api/testcase/export");
        finalResult.put("exportCondition", Map.of(
                "taskId",
                testcaseTaskId,
                "includeDuplicate",
                false
        ));

        AgentTask latest = getAgentTask(agentTaskId);
        latest.setFinalResult(toJson(finalResult));
        latest.setUpdateTime(LocalDateTime.now());
        agentTaskMapper.updateById(latest);
    }

    /**
     * AI 应用专项测试 Agent。
     *
     * 流程：
     * 1. 生成 AI 应用测试用例
     */
    private void runAiAppTestDesign(AgentTask agentTask) {
        int resumeFromStep = normalizeResumeStep(agentTask.getResumeFromStep());

        final String agentTaskId = agentTask.getAgentTaskId();
        final Long projectId = agentTask.getProjectId();
        final String targetVersionNo = agentTask.getTargetVersionNo();
        final String moduleCode = agentTask.getModuleCode();
        final String appType = agentTask.getAppType();
        final String appDescription = agentTask.getAppDescription();
        final String userGoal = agentTask.getUserGoal();
        final String selectedSkillsJson = agentTask.getSelectedSkills();

        AiAppTestGenerateResultVO aiAppResult;

        if (resumeFromStep <= 1) {
            aiAppResult = executeStep(
                    agentTaskId,
                    1,
                    "AI_APP_TEST",
                    "生成 AI 应用专项测试用例",
                    Map.of("appType", appType),
                    () -> {
                        checkCancel(agentTaskId);

                        AgentTask latestTask = getAgentTask(agentTaskId);

                        AiAppTestGenerateRequest request = new AiAppTestGenerateRequest();
                        request.setProjectId(projectId);
                        request.setVersionNo(targetVersionNo);
                        request.setModuleCode(moduleCode);
                        request.setAppType(appType);
                        request.setAppDescription(appDescription);
                        request.setGenerateGoal(userGoal);
                        request.setSelectedSkills(parseSkills(selectedSkillsJson));
                        request.setTestDimensions(parseStringList(latestTask.getTestDimensions()));
                        request.setTopK(safeTopK(latestTask));

                        return aiAppTestService.generate(request);
                    }
            );

            AgentTask latestTask = getAgentTask(agentTaskId);
            latestTask.setAiAppTaskId(aiAppResult.getTaskId());
            latestTask.setUpdateTime(LocalDateTime.now());
            agentTaskMapper.updateById(latestTask);
        }

        String aiAppTaskId = getAgentTask(agentTaskId).getAiAppTaskId();

        if (!StringUtils.hasText(aiAppTaskId)) {
            throw new BusinessException(
                    ErrorCode.AGENT_EXECUTION_ERROR,
                    "缺少 AI 应用测试任务ID，无法继续执行"
            );
        }

        Map<String, Object> finalResult = new LinkedHashMap<>();
        finalResult.put("workflowType", "AI_APP_TEST_DESIGN");
        finalResult.put("aiAppTaskId", aiAppTaskId);
        finalResult.put("queryApi", "/api/ai-app-test/list");
        finalResult.put("queryCondition", Map.of("taskId", aiAppTaskId));

        AgentTask latest = getAgentTask(agentTaskId);
        latest.setFinalResult(toJson(finalResult));
        latest.setUpdateTime(LocalDateTime.now());
        agentTaskMapper.updateById(latest);
    }

    /**
     * 执行单个 Agent 步骤。
     */
    private <T> T executeStep(
            String agentTaskId,
            Integer stepIndex,
            String stepType,
            String stepName,
            Object input,
            StepExecutor<T> executor
    ) {
        checkCancel(agentTaskId);
        checkTaskTimeout(agentTaskId);

        AgentTask task = getAgentTask(agentTaskId);
        task.setCurrentStepIndex(stepIndex);
        task.setUpdateTime(LocalDateTime.now());
        agentTaskMapper.updateById(task);

        AgentTaskStep step = new AgentTaskStep();
        step.setAgentTaskId(agentTaskId);
        step.setStepIndex(stepIndex);
        step.setStepType(stepType);
        step.setStepName(stepName);
        step.setStatus("RUNNING");
        step.setRetryable(1);
        step.setRetryCount(task.getRetryCount() == null ? 0 : task.getRetryCount());
        step.setInput(toJson(input));
        step.setStartTime(LocalDateTime.now());

        agentTaskStepMapper.insert(step);

        long startMillis = System.currentTimeMillis();

        agentExecutionLogService.info(
                agentTaskId,
                stepIndex,
                stepType,
                stepName,
                "STEP_START",
                "步骤开始执行：" + stepName,
                input,
                null,
                null
        );

        try {
            T output = executor.execute();

            long durationMs = System.currentTimeMillis() - startMillis;

            checkStepTimeout(agentTaskId, stepIndex, stepType, stepName, durationMs);

            step.setStatus("SUCCESS");
            step.setOutput(toJson(output));
            step.setEndTime(LocalDateTime.now());
            agentTaskStepMapper.updateById(step);

            agentExecutionLogService.info(
                    agentTaskId,
                    stepIndex,
                    stepType,
                    stepName,
                    "STEP_SUCCESS",
                    "步骤执行成功：" + stepName,
                    input,
                    output,
                    durationMs
            );

            checkCancel(agentTaskId);
            checkTaskTimeout(agentTaskId);

            return output;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startMillis;

            step.setStatus("FAILED");
            step.setErrorMessage(e.getMessage());
            step.setEndTime(LocalDateTime.now());
            agentTaskStepMapper.updateById(step);

            agentExecutionLogService.error(
                    agentTaskId,
                    stepIndex,
                    stepType,
                    stepName,
                    "STEP_FAILED",
                    "步骤执行失败：" + stepName + "，原因：" + e.getMessage(),
                    input,
                    e,
                    durationMs
            );

            throw e;
        }
    }

    /**
     * 创建跳过步骤记录。
     */
    private void createSkippedStep(
            String agentTaskId,
            Integer stepIndex,
            String stepType,
            String stepName,
            String reason
    ) {
        AgentTaskStep step = new AgentTaskStep();
        step.setAgentTaskId(agentTaskId);
        step.setStepIndex(stepIndex);
        step.setStepType(stepType);
        step.setStepName(stepName);
        step.setStatus("SKIPPED");
        step.setRetryable(0);
        step.setInput("{}");
        step.setOutput(toJson(Map.of("reason", reason)));
        step.setStartTime(LocalDateTime.now());
        step.setEndTime(LocalDateTime.now());

        agentTaskStepMapper.insert(step);

        agentExecutionLogService.warn(
                agentTaskId,
                stepIndex,
                stepType,
                stepName,
                "STEP_SKIPPED",
                "步骤被跳过：" + reason,
                null,
                Map.of("reason", reason),
                0L
        );
    }

    private void markAgentRunning(AgentTask task) {
        task.setStatus("RUNNING");
        task.setCancelRequested(0);
        task.setStartTime(task.getStartTime() == null ? LocalDateTime.now() : task.getStartTime());
        task.setUpdateTime(LocalDateTime.now());

        agentTaskMapper.updateById(task);
    }

    private void markAgentFailed(String agentTaskId, String errorMessage) {
        AgentTask task = getAgentTask(agentTaskId);

        if (task.getCancelRequested() != null && task.getCancelRequested() == 1) {
            task.setStatus("CANCELLED");
        } else {
            task.setStatus("FAILED");
        }

        task.setErrorMessage(errorMessage);
        task.setEndTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        agentTaskMapper.updateById(task);
    }

    private void checkCancel(String agentTaskId) {
        AgentTask task = getAgentTask(agentTaskId);

        if (task.getCancelRequested() != null && task.getCancelRequested() == 1) {
            throw new BusinessException(
                    ErrorCode.AGENT_EXECUTION_ERROR,
                    "Agent 任务已被用户取消"
            );
        }
    }

    private AgentTask getAgentTask(String agentTaskId) {
        AgentTask task = agentTaskMapper.selectOne(
                new LambdaQueryWrapper<AgentTask>()
                        .eq(AgentTask::getAgentTaskId, agentTaskId)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "Agent 任务不存在"
            );
        }

        return task;
    }

    private int normalizeResumeStep(Integer resumeFromStep) {
        if (resumeFromStep == null || resumeFromStep <= 0) {
            return 1;
        }

        return resumeFromStep;
    }

    private List<String> parseSkills(String selectedSkillsJson) {
        if (!StringUtils.hasText(selectedSkillsJson) || "null".equals(selectedSkillsJson)) {
            return List.of();
        }

        try {
            return objectMapper.readValue(
                    selectedSkillsJson,
                    new TypeReference<List<String>>() {
                    }
            );
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<String> parseStringList(String json) {
        if (!StringUtils.hasText(json) || "null".equals(json)) {
            return List.of();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<String>>() {
                    }
            );
        } catch (Exception e) {
            return List.of();
        }
    }

    private int safeTopK(AgentTask task) {
        return task.getTopK() == null || task.getTopK() <= 0
                ? 8
                : task.getTopK();
    }

    private double safeDeduplicateThreshold(AgentTask task) {
        return task.getDeduplicateThreshold() == null
                ? 0.85
                : task.getDeduplicateThreshold();
    }

    private boolean enabled(Integer value) {
        return value == null || value == 1;
    }

    private String toJson(Object object) {
        try {
            if (object == null) {
                return "null";
            }

            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "JSON 序列化失败：" + e.getMessage()
            );
        }
    }

    private void checkTaskTimeout(String agentTaskId) {
        AgentTask task = getAgentTask(agentTaskId);

        if (task.getStartTime() == null) {
            return;
        }

        long elapsedSeconds = java.time.Duration.between(
                task.getStartTime(),
                LocalDateTime.now()
        ).toSeconds();

        Long timeoutSeconds = agentExecutionProperties.getTaskTimeoutSeconds();

        if (timeoutSeconds != null && elapsedSeconds > timeoutSeconds) {
            throw new BusinessException(
                    ErrorCode.AGENT_EXECUTION_ERROR,
                    "Agent 任务执行超时，已执行 "
                            + elapsedSeconds
                            + " 秒，超过限制 "
                            + timeoutSeconds
                            + " 秒"
            );
        }
    }

    private void checkStepTimeout(
            String agentTaskId,
            Integer stepIndex,
            String stepType,
            String stepName,
            long durationMs
    ) {
        Long timeoutSeconds = agentExecutionProperties.getStepTimeoutSeconds();

        if (timeoutSeconds == null) {
            return;
        }

        long timeoutMs = timeoutSeconds * 1000;

        if (durationMs > timeoutMs) {
            agentExecutionLogService.warn(
                    agentTaskId,
                    stepIndex,
                    stepType,
                    stepName,
                    "TIMEOUT",
                    "步骤执行耗时超过阈值，durationMs="
                            + durationMs
                            + "，timeoutMs="
                            + timeoutMs,
                    null,
                    null,
                    durationMs
            );
        }
    }

    @FunctionalInterface
    private interface StepExecutor<T> {

        /**
         * 执行步骤。
         */
        T execute();
    }
}