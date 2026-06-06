package com.zy.testpilotai.requirement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.vo.RagContextVO;
import com.zy.testpilotai.knowledge.service.KnowledgeBaseService;
import com.zy.testpilotai.llm.chat.LlmClient;
import com.zy.testpilotai.llm.structured.AiRequirementImpactOutputParser;
import com.zy.testpilotai.llm.structured.AiTestCaseOutputParser;
import com.zy.testpilotai.llm.structured.dto.AiGeneratedTestCaseItemDTO;
import com.zy.testpilotai.llm.structured.dto.AiGeneratedTestCaseOutputDTO;
import com.zy.testpilotai.llm.structured.dto.AiRequirementImpactOutputDTO;
import com.zy.testpilotai.project.service.ProjectService;
import com.zy.testpilotai.requirement.mapper.RequirementChangeAnalysisTaskMapper;
import com.zy.testpilotai.requirement.model.dto.ChangeImpactAnalyzeRequest;
import com.zy.testpilotai.requirement.model.dto.IncrementalTestCaseGenerateRequest;
import com.zy.testpilotai.requirement.model.entity.RequirementChangeAnalysisTask;
import com.zy.testpilotai.requirement.model.vo.ChangeImpactAnalyzeResultVO;
import com.zy.testpilotai.requirement.model.vo.IncrementalTestCaseGenerateResultVO;
import com.zy.testpilotai.requirement.prompt.RequirementChangePromptBuilder;
import com.zy.testpilotai.requirement.service.RequirementChangeService;
import com.zy.testpilotai.testcase.mapper.TestCaseGenerateTaskMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseMapper;
import com.zy.testpilotai.testcase.model.entity.TestCase;
import com.zy.testpilotai.testcase.model.entity.TestCaseGenerateTask;
import com.zy.testpilotai.testcase.model.vo.TestCaseVO;
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
public class RequirementChangeServiceImpl implements RequirementChangeService {

    private final ProjectService projectService;

    private final KnowledgeBaseService knowledgeBaseService;

    private final LlmClient llmClient;

    private final RequirementChangePromptBuilder promptBuilder;

    private final RequirementChangeAnalysisTaskMapper analysisTaskMapper;

    private final TestCaseMapper testCaseMapper;

    private final TestCaseGenerateTaskMapper testCaseGenerateTaskMapper;

    private final ObjectMapper objectMapper;

    private final AiTestCaseOutputParser aiTestCaseOutputParser;

    private final AiRequirementImpactOutputParser aiRequirementImpactOutputParser;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChangeImpactAnalyzeResultVO analyzeImpact(ChangeImpactAnalyzeRequest request) {
        // 1. 校验项目是否存在
        projectService.getById(request.getProjectId());

        String analysisTaskId = "ia_" + UUID.randomUUID().toString().replace("-", "");

        RequirementChangeAnalysisTask task = createAnalysisTask(analysisTaskId, request);

        try {
            // 2. 检索基线版本相关知识库内容
            KnowledgeSearchRequest searchRequest = new KnowledgeSearchRequest();
            searchRequest.setProjectId(request.getProjectId());
            searchRequest.setVersionNo(request.getBaseVersionNo());
            searchRequest.setQuery(request.getNewRequirement());
            searchRequest.setTopK(request.getTopK());

            RagContextVO oldVersionContext = knowledgeBaseService.buildRagContext(searchRequest);

            // 3. 查询基线版本历史测试用例
            List<TestCase> historicalCases = listHistoricalCases(
                    request.getProjectId(),
                    request.getBaseVersionNo(),
                    null
            );

            // 4. 构建影响分析 Prompt
            String systemPrompt = promptBuilder.buildImpactSystemPrompt();
            String userPrompt = promptBuilder.buildImpactUserPrompt(
                    request,
                    oldVersionContext,
                    historicalCases
            );

            // 5. 调用 LLM 做新需求影响分析
            String rawOutput = llmClient.chat(
                    systemPrompt,
                    userPrompt,
                    "REQUIREMENT_IMPACT",
                    analysisTaskId
            );

            /*
             * 6. 使用结构化解析器解析影响分析结果。
             */
            AiRequirementImpactOutputDTO impactOutput =
                    aiRequirementImpactOutputParser.parse(rawOutput);

            // 7. 更新影响分析任务
            task.setChangeSummary(impactOutput.getChangeSummary());
            task.setAffectedModules(impactOutput.getAffectedModules());
            task.setRelatedOldRules(impactOutput.getRelatedOldRules());
            task.setRiskPoints(impactOutput.getRiskPoints());
            task.setRegressionScope(impactOutput.getRegressionScope());
            task.setSuggestedNewTestPoints(impactOutput.getSuggestedNewTestPoints());
            task.setRawModelOutput(rawOutput);
            task.setStatus("SUCCESS");
            task.setUpdateTime(LocalDateTime.now());

            analysisTaskMapper.updateById(task);

            return toImpactVO(task);
        } catch (BusinessException e) {
            markAnalysisFailed(task, e.getMessage());
            throw e;
        } catch (Exception e) {
            markAnalysisFailed(task, e.getMessage());
            throw new BusinessException(
                    ErrorCode.REQUIREMENT_IMPACT_ERROR,
                    "新需求影响分析失败：" + e.getMessage()
            );
        }
    }

    @Override
    public ChangeImpactAnalyzeResultVO getImpactTask(String analysisTaskId) {
        RequirementChangeAnalysisTask task = getAnalysisTask(analysisTaskId);
        return toImpactVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IncrementalTestCaseGenerateResultVO generateIncrementalCases(
            IncrementalTestCaseGenerateRequest request
    ) {
        RequirementChangeAnalysisTask analysisTask = getAnalysisTask(request.getAnalysisTaskId());

        if (!"SUCCESS".equals(analysisTask.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "影响分析任务未成功，不能生成增量测试用例"
            );
        }

        String taskId = "tc_inc_" + UUID.randomUUID().toString().replace("-", "");

        TestCaseGenerateTask generateTask = createIncrementalGenerateTask(taskId, analysisTask, request);

        try {
            // 1. 基于新需求和影响分析结果，再检索一次旧版本知识库
            KnowledgeSearchRequest searchRequest = new KnowledgeSearchRequest();
            searchRequest.setProjectId(analysisTask.getProjectId());
            searchRequest.setVersionNo(analysisTask.getBaseVersionNo());
            searchRequest.setQuery(
                    analysisTask.getNewRequirement()
                            + "\n影响模块："
                            + analysisTask.getAffectedModules()
                            + "\n风险点："
                            + analysisTask.getRiskPoints()
            );
            searchRequest.setTopK(request.getTopK());

            RagContextVO oldVersionContext = knowledgeBaseService.buildRagContext(searchRequest);

            // 2. 查询历史测试用例，用于避免重复和生成回归用例
            List<TestCase> historicalCases = listHistoricalCases(
                    analysisTask.getProjectId(),
                    analysisTask.getBaseVersionNo(),
                    null
            );

            // 3. 构建增量测试用例生成 Prompt
            String systemPrompt = promptBuilder.buildIncrementalSystemPrompt();
            String userPrompt = promptBuilder.buildIncrementalUserPrompt(
                    request,
                    analysisTask,
                    oldVersionContext,
                    historicalCases
            );

            // 4. 调用 LLM 生成增量测试用例
            String rawOutput = llmClient.chat(
                    systemPrompt,
                    userPrompt,
                    "INCREMENTAL_TESTCASE_GENERATE",
                    taskId
            );

            // 5. 保存模型原始输出到生成任务
            generateTask.setRawModelOutput(rawOutput);
            generateTask.setUpdateTime(LocalDateTime.now());
            testCaseGenerateTaskMapper.updateById(generateTask);

            /*
             * 6. 结构化解析增量测试用例。
             *
             * 这里走 AiTestCaseOutputParser，
             * 可以兼容 steps / testData / sourceReferences 输出成对象、数组或字符串。
             */
            List<TestCase> testCases = parseIncrementalTestCases(
                    rawOutput,
                    taskId,
                    analysisTask
            );

            if (testCases.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.INCREMENTAL_TESTCASE_ERROR,
                        "模型没有生成任何增量测试用例"
                );
            }

            // 7. 入库测试用例
            for (TestCase testCase : testCases) {
                testCaseMapper.insert(testCase);
            }

            // 8. 更新生成任务状态
            generateTask.setStatus("SUCCESS");
            generateTask.setUpdateTime(LocalDateTime.now());
            testCaseGenerateTaskMapper.updateById(generateTask);

            IncrementalTestCaseGenerateResultVO resultVO = new IncrementalTestCaseGenerateResultVO();
            resultVO.setTaskId(taskId);
            resultVO.setAnalysisTaskId(analysisTask.getAnalysisTaskId());
            resultVO.setProjectId(analysisTask.getProjectId());
            resultVO.setBaseVersionNo(analysisTask.getBaseVersionNo());
            resultVO.setTargetVersionNo(analysisTask.getTargetVersionNo());
            resultVO.setCaseCount(testCases.size());
            resultVO.setTestCases(testCases.stream().map(this::toTestCaseVO).toList());
            resultVO.setRawModelOutput(rawOutput);

            return resultVO;
        } catch (BusinessException e) {
            markGenerateTaskFailed(generateTask, e.getMessage());
            throw e;
        } catch (Exception e) {
            markGenerateTaskFailed(generateTask, e.getMessage());
            throw new BusinessException(
                    ErrorCode.INCREMENTAL_TESTCASE_ERROR,
                    "增量测试用例生成失败：" + e.getMessage()
            );
        }
    }

    private RequirementChangeAnalysisTask createAnalysisTask(
            String analysisTaskId,
            ChangeImpactAnalyzeRequest request
    ) {
        RequirementChangeAnalysisTask task = new RequirementChangeAnalysisTask();

        task.setAnalysisTaskId(analysisTaskId);
        task.setProjectId(request.getProjectId());
        task.setBaseVersionNo(request.getBaseVersionNo());
        task.setTargetVersionNo(request.getTargetVersionNo());
        task.setNewRequirement(request.getNewRequirement());
        task.setStatus("RUNNING");
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        analysisTaskMapper.insert(task);

        return task;
    }

    private TestCaseGenerateTask createIncrementalGenerateTask(
            String taskId,
            RequirementChangeAnalysisTask analysisTask,
            IncrementalTestCaseGenerateRequest request
    ) {
        try {
            TestCaseGenerateTask task = new TestCaseGenerateTask();

            task.setTaskId(taskId);
            task.setProjectId(analysisTask.getProjectId());

            // 增量用例归属目标版本，而不是基线版本
            task.setVersionNo(analysisTask.getTargetVersionNo());

            task.setModuleCode(null);
            task.setGenerateGoal("基于新需求生成增量测试用例：" + analysisTask.getNewRequirement());
            task.setGenerateType("INCREMENTAL");
            task.setSelectedSkills(objectMapper.writeValueAsString(request.getSelectedSkills()));
            task.setStatus("RUNNING");
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            testCaseGenerateTaskMapper.insert(task);

            return task;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "创建增量测试用例生成任务失败：" + e.getMessage()
            );
        }
    }

    private RequirementChangeAnalysisTask getAnalysisTask(String analysisTaskId) {
        RequirementChangeAnalysisTask task = analysisTaskMapper.selectOne(
                new LambdaQueryWrapper<RequirementChangeAnalysisTask>()
                        .eq(RequirementChangeAnalysisTask::getAnalysisTaskId, analysisTaskId)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "影响分析任务不存在"
            );
        }

        return task;
    }

    private List<TestCase> listHistoricalCases(
            Long projectId,
            String versionNo,
            String moduleCode
    ) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<TestCase>()
                .eq(TestCase::getProjectId, projectId)
                .eq(TestCase::getVersionNo, versionNo)
                .orderByDesc(TestCase::getCreateTime)
                .last("LIMIT 30");

        if (StringUtils.hasText(moduleCode)) {
            wrapper.eq(TestCase::getModuleCode, moduleCode);
        }

        return testCaseMapper.selectList(wrapper);
    }

    private List<TestCase> parseIncrementalTestCases(
            String rawOutput,
            String taskId,
            RequirementChangeAnalysisTask analysisTask
    ) {
        AiGeneratedTestCaseOutputDTO output = aiTestCaseOutputParser.parse(rawOutput);

        List<TestCase> result = new ArrayList<>();

        for (AiGeneratedTestCaseItemDTO item : output.getTestCases()) {
            TestCase testCase = new TestCase();

            // 增量用例归属目标版本
            testCase.setTaskId(taskId);
            testCase.setProjectId(analysisTask.getProjectId());
            testCase.setVersionNo(analysisTask.getTargetVersionNo());

            testCase.setModuleCode(item.getModuleCode());
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

            // 增量生成用例默认未评分
            testCase.setQualityScore(null);

            // 默认不重复
            testCase.setDuplicateStatus("NORMAL");
            testCase.setDuplicateOfCaseId(null);
            testCase.setDuplicateScore(null);
            testCase.setDuplicateReason(null);

            // 增量需求生成来源
            testCase.setSourceType("AI_INCREMENTAL");

            testCase.setCreateTime(LocalDateTime.now());
            testCase.setUpdateTime(LocalDateTime.now());

            result.add(testCase);
        }

        return result;
    }

    private void markAnalysisFailed(
            RequirementChangeAnalysisTask task,
            String errorMessage
    ) {
        task.setStatus("FAILED");
        task.setErrorMessage(errorMessage);
        task.setUpdateTime(LocalDateTime.now());

        analysisTaskMapper.updateById(task);
    }

    private void markGenerateTaskFailed(
            TestCaseGenerateTask task,
            String errorMessage
    ) {
        task.setStatus("FAILED");
        task.setErrorMessage(errorMessage);
        task.setUpdateTime(LocalDateTime.now());

        testCaseGenerateTaskMapper.updateById(task);
    }

    private ChangeImpactAnalyzeResultVO toImpactVO(
            RequirementChangeAnalysisTask task
    ) {
        ChangeImpactAnalyzeResultVO vo = new ChangeImpactAnalyzeResultVO();

        vo.setAnalysisTaskId(task.getAnalysisTaskId());
        vo.setProjectId(task.getProjectId());
        vo.setBaseVersionNo(task.getBaseVersionNo());
        vo.setTargetVersionNo(task.getTargetVersionNo());
        vo.setNewRequirement(task.getNewRequirement());
        vo.setStatus(task.getStatus());

        vo.setChangeSummary(task.getChangeSummary());
        vo.setAffectedModules(task.getAffectedModules());
        vo.setRelatedOldRules(task.getRelatedOldRules());
        vo.setRelatedHistoricalCases(task.getRelatedHistoricalCases());
        vo.setRiskPoints(task.getRiskPoints());
        vo.setRegressionScope(task.getRegressionScope());
        vo.setSuggestedNewTestPoints(task.getSuggestedNewTestPoints());
        vo.setRawModelOutput(task.getRawModelOutput());
        vo.setErrorMessage(task.getErrorMessage());

        return vo;
    }

    private TestCaseVO toTestCaseVO(TestCase testCase) {
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