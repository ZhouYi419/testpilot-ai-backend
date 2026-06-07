package com.zy.testpilotai.agent.planner.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.agent.planner.mapper.AgentPlanStepMapper;
import com.zy.testpilotai.agent.planner.mapper.AgentPlanTaskMapper;
import com.zy.testpilotai.agent.planner.model.dto.AgentPlanCreateRequest;
import com.zy.testpilotai.agent.planner.model.dto.AgentPlanExecuteRequest;
import com.zy.testpilotai.agent.planner.model.dto.AgentPlanQueryRequest;
import com.zy.testpilotai.agent.planner.model.dto.AgentPlanRetryStepRequest;
import com.zy.testpilotai.agent.planner.model.dto.AiAgentPlanOutputDTO;
import com.zy.testpilotai.agent.planner.model.dto.AiAgentPlanStepDTO;
import com.zy.testpilotai.agent.planner.model.entity.AgentPlanStep;
import com.zy.testpilotai.agent.planner.model.entity.AgentPlanTask;
import com.zy.testpilotai.agent.planner.model.vo.AgentPlanDetailVO;
import com.zy.testpilotai.agent.planner.model.vo.AgentPlanStepVO;
import com.zy.testpilotai.agent.planner.model.vo.AgentPlanTaskVO;
import com.zy.testpilotai.agent.planner.model.vo.AgentToolInfoVO;
import com.zy.testpilotai.agent.planner.parser.AgentPlanOutputParser;
import com.zy.testpilotai.agent.planner.service.AgentPlannerService;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutionResult;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutor;
import com.zy.testpilotai.agent.planner.tool.AgentToolRegistry;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.llm.chat.LlmClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentPlannerServiceImpl implements AgentPlannerService {

    private final AgentPlanTaskMapper agentPlanTaskMapper;

    private final AgentPlanStepMapper agentPlanStepMapper;

    private final AgentToolRegistry agentToolRegistry;

    private final LlmClient llmClient;

    private final AgentPlanOutputParser agentPlanOutputParser;

    private final ObjectMapper objectMapper;

    @Override
    public List<AgentToolInfoVO> listTools() {
        return agentToolRegistry.listTools();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentPlanDetailVO createPlan(AgentPlanCreateRequest request) {
        validateCreateRequest(request);

        String planTaskId = "ap_" + UUID.randomUUID().toString().replace("-", "");
        String planningMode = normalizePlanningMode(request.getPlanningMode());

        List<String> allowedTools = resolveAllowedTools(request.getAllowedTools());

        AgentPlanTask task = createPlanningTask(
                planTaskId,
                request,
                planningMode,
                allowedTools
        );

        try {
            AiAgentPlanOutputDTO planOutput;

            if ("TEMPLATE".equals(planningMode)) {
                planOutput = buildTemplatePlan(request, allowedTools);
                task.setRawModelOutput(toJson(planOutput));
            } else {
                String systemPrompt = buildSystemPrompt(allowedTools);
                String userPrompt = buildUserPrompt(request, allowedTools);

                String rawOutput = llmClient.chat(
                        systemPrompt,
                        userPrompt,
                        "AGENT_PLANNER",
                        planTaskId
                );

                task.setRawModelOutput(rawOutput);
                planOutput = agentPlanOutputParser.parse(rawOutput);
            }

            validatePlanOutput(planOutput, allowedTools);

            savePlanSteps(planTaskId, planOutput);

            task.setPlanJson(toJson(planOutput));
            task.setStatus(Boolean.TRUE.equals(request.getAutoExecute()) ? "RUNNING" : "WAITING_CONFIRM");
            task.setTotalStepCount(planOutput.getSteps().size());
            task.setUpdateTime(LocalDateTime.now());

            agentPlanTaskMapper.updateById(task);

            if (Boolean.TRUE.equals(request.getAutoExecute())) {
                executePlan(planTaskId);
            }

            return detail(planTaskId);
        } catch (BusinessException e) {
            markTaskFailed(task, e.getMessage());
            throw e;
        } catch (Exception e) {
            markTaskFailed(task, e.getMessage());
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "Agent 计划创建失败：" + e.getMessage()
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentPlanDetailVO approveAndExecute(AgentPlanExecuteRequest request) {
        if (request == null || !StringUtils.hasText(request.getPlanTaskId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "计划任务 ID 不能为空"
            );
        }

        AgentPlanTask task = getTask(request.getPlanTaskId());

        if (!"WAITING_CONFIRM".equals(task.getStatus())
                && !"FAILED".equals(task.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "当前计划状态不允许确认执行，status=" + task.getStatus()
            );
        }

        task.setApproved(1);
        task.setStatus("RUNNING");
        task.setStartTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        agentPlanTaskMapper.updateById(task);

        executePlan(task.getPlanTaskId());

        return detail(task.getPlanTaskId());
    }

    @Override
    public List<AgentPlanTaskVO> list(AgentPlanQueryRequest request) {
        LambdaQueryWrapper<AgentPlanTask> wrapper =
                new LambdaQueryWrapper<AgentPlanTask>()
                        .orderByDesc(AgentPlanTask::getCreateTime)
                        .orderByDesc(AgentPlanTask::getId);

        if (request != null && request.getProjectId() != null) {
            wrapper.eq(AgentPlanTask::getProjectId, request.getProjectId());
        }

        if (request != null && StringUtils.hasText(request.getVersionNo())) {
            wrapper.eq(AgentPlanTask::getVersionNo, request.getVersionNo());
        }

        if (request != null && StringUtils.hasText(request.getModuleCode())) {
            wrapper.eq(AgentPlanTask::getModuleCode, request.getModuleCode());
        }

        if (request != null && StringUtils.hasText(request.getStatus())) {
            wrapper.eq(AgentPlanTask::getStatus, request.getStatus());
        }

        if (request != null && StringUtils.hasText(request.getKeyword())) {
            wrapper.like(AgentPlanTask::getUserGoal, request.getKeyword());
        }

        return agentPlanTaskMapper.selectList(wrapper)
                .stream()
                .map(this::toTaskVO)
                .toList();
    }

    @Override
    public AgentPlanDetailVO detail(String planTaskId) {
        if (!StringUtils.hasText(planTaskId)) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "计划任务 ID 不能为空"
            );
        }

        AgentPlanTask task = getTask(planTaskId);

        List<AgentPlanStepVO> steps =
                agentPlanStepMapper.selectList(
                                new LambdaQueryWrapper<AgentPlanStep>()
                                        .eq(AgentPlanStep::getPlanTaskId, planTaskId)
                                        .orderByAsc(AgentPlanStep::getStepIndex)
                        )
                        .stream()
                        .map(this::toStepVO)
                        .toList();

        AgentPlanDetailVO detailVO = new AgentPlanDetailVO();
        detailVO.setTask(toTaskVO(task));
        detailVO.setSteps(steps);

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentPlanDetailVO retryStep(AgentPlanRetryStepRequest request) {
        if (request == null || !StringUtils.hasText(request.getPlanTaskId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "计划任务 ID 不能为空"
            );
        }

        if (request.getStepIndex() == null || request.getStepIndex() <= 0) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "步骤序号不能为空"
            );
        }

        AgentPlanTask task = getTask(request.getPlanTaskId());

        AgentPlanStep step = getStep(request.getPlanTaskId(), request.getStepIndex());

        if (!"FAILED".equals(step.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "只有失败步骤可以重试"
            );
        }

        task.setStatus("RUNNING");
        task.setUpdateTime(LocalDateTime.now());
        agentPlanTaskMapper.updateById(task);

        step.setStatus("PENDING");
        step.setErrorMessage(null);
        step.setOutputJson(null);
        step.setRetryCount(step.getRetryCount() == null ? 1 : step.getRetryCount() + 1);
        step.setUpdateTime(LocalDateTime.now());
        agentPlanStepMapper.updateById(step);

        executeSingleStep(task, step);
        refreshTaskResult(task.getPlanTaskId());

        return detail(task.getPlanTaskId());
    }

    private void executePlan(String planTaskId) {
        AgentPlanTask task = getTask(planTaskId);

        List<AgentPlanStep> steps =
                agentPlanStepMapper.selectList(
                        new LambdaQueryWrapper<AgentPlanStep>()
                                .eq(AgentPlanStep::getPlanTaskId, planTaskId)
                                .orderByAsc(AgentPlanStep::getStepIndex)
                );

        if (steps.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "计划步骤为空，无法执行"
            );
        }

        task.setStatus("RUNNING");
        task.setStartTime(task.getStartTime() == null ? LocalDateTime.now() : task.getStartTime());
        task.setUpdateTime(LocalDateTime.now());
        agentPlanTaskMapper.updateById(task);

        for (AgentPlanStep step : steps) {
            if ("SUCCESS".equals(step.getStatus())) {
                continue;
            }

            executeSingleStep(task, step);

            AgentPlanStep latestStep = getStep(planTaskId, step.getStepIndex());

            if ("FAILED".equals(latestStep.getStatus())) {
                break;
            }
        }

        refreshTaskResult(planTaskId);
    }

    private void executeSingleStep(
            AgentPlanTask task,
            AgentPlanStep step
    ) {
        try {
            step.setStatus("RUNNING");
            step.setStartTime(LocalDateTime.now());
            step.setUpdateTime(LocalDateTime.now());
            agentPlanStepMapper.updateById(step);

            task.setCurrentStepIndex(step.getStepIndex());
            task.setUpdateTime(LocalDateTime.now());
            agentPlanTaskMapper.updateById(task);

            Map<String, Object> inputParams = parseMap(step.getInputParams());

            AgentToolExecutor executor = agentToolRegistry.get(step.getToolName());
            AgentToolExecutionResult result = executor.execute(inputParams);

            if (!Boolean.TRUE.equals(result.getSuccess())) {
                throw new BusinessException(
                        ErrorCode.SYSTEM_ERROR,
                        result.getMessage()
                );
            }

            step.setStatus("SUCCESS");
            step.setOutputJson(toJson(result));
            step.setEndTime(LocalDateTime.now());
            step.setUpdateTime(LocalDateTime.now());
            agentPlanStepMapper.updateById(step);
        } catch (Exception e) {
            step.setStatus("FAILED");
            step.setErrorMessage(e.getMessage());
            step.setEndTime(LocalDateTime.now());
            step.setUpdateTime(LocalDateTime.now());
            agentPlanStepMapper.updateById(step);
        }
    }

    private void refreshTaskResult(String planTaskId) {
        AgentPlanTask task = getTask(planTaskId);

        List<AgentPlanStep> steps =
                agentPlanStepMapper.selectList(
                        new LambdaQueryWrapper<AgentPlanStep>()
                                .eq(AgentPlanStep::getPlanTaskId, planTaskId)
                                .orderByAsc(AgentPlanStep::getStepIndex)
                );

        int successCount = 0;
        int failedCount = 0;
        int pendingCount = 0;

        List<Map<String, Object>> stepResults = new ArrayList<>();

        for (AgentPlanStep step : steps) {
            if ("SUCCESS".equals(step.getStatus())) {
                successCount++;
            } else if ("FAILED".equals(step.getStatus())) {
                failedCount++;
            } else {
                pendingCount++;
            }

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("stepIndex", step.getStepIndex());
            item.put("toolName", step.getToolName());
            item.put("stepName", step.getStepName());
            item.put("status", step.getStatus());
            item.put("outputJson", step.getOutputJson());
            item.put("errorMessage", step.getErrorMessage());
            stepResults.add(item);
        }

        task.setSuccessStepCount(successCount);
        task.setFailedStepCount(failedCount);
        task.setTotalStepCount(steps.size());
        task.setFinalResult(toJson(Map.of("steps", stepResults)));

        if (failedCount > 0) {
            task.setStatus("FAILED");
            task.setErrorMessage("存在失败步骤，请查看 agent_plan_step");
            task.setEndTime(LocalDateTime.now());
        } else if (pendingCount == 0) {
            task.setStatus("SUCCESS");
            task.setErrorMessage(null);
            task.setEndTime(LocalDateTime.now());
        } else {
            task.setStatus("RUNNING");
        }

        task.setUpdateTime(LocalDateTime.now());
        agentPlanTaskMapper.updateById(task);
    }

    private AgentPlanTask createPlanningTask(
            String planTaskId,
            AgentPlanCreateRequest request,
            String planningMode,
            List<String> allowedTools
    ) {
        AgentPlanTask task = new AgentPlanTask();

        task.setPlanTaskId(planTaskId);
        task.setProjectId(request.getProjectId());
        task.setVersionNo(request.getVersionNo());
        task.setModuleCode(request.getModuleCode());
        task.setUserGoal(request.getUserGoal());
        task.setPlanningMode(planningMode);
        task.setAllowedTools(toJson(allowedTools));
        task.setStatus("PLANNING");
        task.setApproved(0);
        task.setCurrentStepIndex(0);
        task.setTotalStepCount(0);
        task.setSuccessStepCount(0);
        task.setFailedStepCount(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        agentPlanTaskMapper.insert(task);

        return task;
    }

    private void savePlanSteps(
            String planTaskId,
            AiAgentPlanOutputDTO planOutput
    ) {
        int index = 1;

        for (AiAgentPlanStepDTO stepDTO : planOutput.getSteps()) {
            AgentPlanStep step = new AgentPlanStep();

            step.setPlanTaskId(planTaskId);
            step.setStepIndex(index++);
            step.setToolName(stepDTO.getToolName());
            step.setStepName(stepDTO.getStepName());
            step.setStepGoal(stepDTO.getStepGoal());
            step.setInputParams(toJson(stepDTO.getInputParams() == null ? Map.of() : stepDTO.getInputParams()));
            step.setStatus("PENDING");
            step.setRetryCount(0);
            step.setCreateTime(LocalDateTime.now());
            step.setUpdateTime(LocalDateTime.now());

            agentPlanStepMapper.insert(step);
        }
    }

    private AiAgentPlanOutputDTO buildTemplatePlan(
            AgentPlanCreateRequest request,
            List<String> allowedTools
    ) {
        Map<String, Object> context = request.getContext() == null
                ? new LinkedHashMap<>()
                : new LinkedHashMap<>(request.getContext());

        if (request.getProjectId() != null) {
            context.putIfAbsent("projectId", request.getProjectId());
        }

        if (StringUtils.hasText(request.getVersionNo())) {
            context.putIfAbsent("versionNo", request.getVersionNo());
        }

        if (StringUtils.hasText(request.getModuleCode())) {
            context.putIfAbsent("moduleCode", request.getModuleCode());
        }

        String goal = request.getUserGoal();

        AiAgentPlanOutputDTO output = new AiAgentPlanOutputDTO();
        output.setPlanName("模板执行计划");
        output.setPlanDescription("根据用户目标关键词生成的兜底计划");

        if (contains(goal, "知识库", "质量", "评估")) {
            addStep(output, allowedTools, "evaluateKnowledgeTool", "评估知识库质量", "检查知识库构建质量", context);
        } else if (contains(goal, "RAG", "评测", "召回")) {
            addStep(output, allowedTools, "runRagEvalTool", "运行 RAG 评测", "评估 RAG 召回效果", context);
        } else if (contains(goal, "AI应用", "Prompt", "注入", "幻觉", "越权")) {
            addStep(output, allowedTools, "runAiEvalTool", "运行 AI 应用测试", "评估 AI 应用质量和安全风险", context);
        } else if (contains(goal, "自动化", "脚本", "生成")) {
            addStep(output, allowedTools, "generateAutomationScriptTool", "生成自动化脚本", "生成 pytest + requests 脚本", context);
        } else if (contains(goal, "执行", "pytest", "自动化")) {
            addStep(output, allowedTools, "runAutomationScriptTool", "执行自动化脚本", "启动 pytest 自动化执行任务", context);
        } else if (contains(goal, "去重", "重复")) {
            addStep(output, allowedTools, "semanticDeduplicateTool", "语义去重", "对测试用例进行语义去重", context);
        } else if (contains(goal, "对比", "版本", "用例集")) {
            addStep(output, allowedTools, "compareCaseSetTool", "用例集版本对比", "对比两个用例集差异", context);
        } else {
            context.putIfAbsent("query", goal);
            addStep(output, allowedTools, "searchKnowledgeTool", "检索知识库", "根据目标检索相关知识库上下文", context);
        }

        return output;
    }

    private void addStep(
            AiAgentPlanOutputDTO output,
            List<String> allowedTools,
            String toolName,
            String stepName,
            String stepGoal,
            Map<String, Object> inputParams
    ) {
        if (!allowedTools.contains(toolName)) {
            return;
        }

        AiAgentPlanStepDTO step = new AiAgentPlanStepDTO();
        step.setToolName(toolName);
        step.setStepName(stepName);
        step.setStepGoal(stepGoal);
        step.setInputParams(new LinkedHashMap<>(inputParams));

        output.getSteps().add(step);
    }

    private void validatePlanOutput(
            AiAgentPlanOutputDTO planOutput,
            List<String> allowedTools
    ) {
        if (planOutput == null || CollectionUtils.isEmpty(planOutput.getSteps())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "Agent 计划步骤不能为空"
            );
        }

        for (AiAgentPlanStepDTO step : planOutput.getSteps()) {
            if (!StringUtils.hasText(step.getToolName())) {
                throw new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "Agent 计划中存在空工具名称"
                );
            }

            if (!agentToolRegistry.contains(step.getToolName())) {
                throw new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "Agent 计划包含未注册工具：" + step.getToolName()
                );
            }

            if (!allowedTools.contains(step.getToolName())) {
                throw new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "Agent 计划包含未授权工具：" + step.getToolName()
                );
            }
        }
    }

    private String buildSystemPrompt(List<String> allowedTools) {
        return """
                你是 TestPilot AI 的 Agent Planner。
                你的职责是根据用户目标生成执行计划，但你不能直接执行工具。
                后端会根据你的计划进行白名单校验并执行。
                
                你必须严格遵守：
                1. 只输出 JSON，不要输出 Markdown。
                2. steps 里的 toolName 只能使用 allowedTools 中的工具。
                3. 不要编造工具名称。
                4. 如果参数不确定，可以从用户提供的 context 中取值。
                5. 每一步只做一个明确动作。
                6. 输出格式必须是：
                {
                  "planName": "计划名称",
                  "planDescription": "计划说明",
                  "steps": [
                    {
                      "stepName": "步骤名称",
                      "toolName": "工具名称",
                      "stepGoal": "步骤目标",
                      "inputParams": {
                        "projectId": 1
                      }
                    }
                  ]
                }
                
                当前允许使用的工具：
                """
                + toJson(allowedTools)
                + "\n\n工具说明：\n"
                + toJson(agentToolRegistry.listTools());
    }

    private String buildUserPrompt(
            AgentPlanCreateRequest request,
            List<String> allowedTools
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("projectId", request.getProjectId());
        payload.put("versionNo", request.getVersionNo());
        payload.put("moduleCode", request.getModuleCode());
        payload.put("userGoal", request.getUserGoal());
        payload.put("allowedTools", allowedTools);
        payload.put("context", request.getContext() == null ? Map.of() : request.getContext());

        return "请根据下面输入生成 Agent 执行计划：\n" + toJson(payload);
    }

    private void validateCreateRequest(AgentPlanCreateRequest request) {
        if (request == null || !StringUtils.hasText(request.getUserGoal())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "用户目标不能为空"
            );
        }
    }

    private List<String> resolveAllowedTools(List<String> requestedAllowedTools) {
        List<String> allTools = agentToolRegistry.toolNames();

        if (CollectionUtils.isEmpty(requestedAllowedTools)) {
            return allTools;
        }

        for (String tool : requestedAllowedTools) {
            if (!agentToolRegistry.contains(tool)) {
                throw new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "allowedTools 包含未注册工具：" + tool
                );
            }
        }

        return requestedAllowedTools;
    }

    private String normalizePlanningMode(String planningMode) {
        if (!StringUtils.hasText(planningMode)) {
            return "LLM";
        }

        String value = planningMode.trim().toUpperCase();

        return switch (value) {
            case "LLM", "TEMPLATE" -> value;
            default -> "LLM";
        };
    }

    private boolean contains(String text, String... keywords) {
        if (!StringUtils.hasText(text)) {
            return false;
        }

        for (String keyword : keywords) {
            if (text.toLowerCase().contains(keyword.toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    private AgentPlanTask getTask(String planTaskId) {
        AgentPlanTask task =
                agentPlanTaskMapper.selectOne(
                        new LambdaQueryWrapper<AgentPlanTask>()
                                .eq(AgentPlanTask::getPlanTaskId, planTaskId)
                                .last("LIMIT 1")
                );

        if (task == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "Agent 计划任务不存在"
            );
        }

        return task;
    }

    private AgentPlanStep getStep(String planTaskId, Integer stepIndex) {
        AgentPlanStep step =
                agentPlanStepMapper.selectOne(
                        new LambdaQueryWrapper<AgentPlanStep>()
                                .eq(AgentPlanStep::getPlanTaskId, planTaskId)
                                .eq(AgentPlanStep::getStepIndex, stepIndex)
                                .last("LIMIT 1")
                );

        if (step == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "Agent 计划步骤不存在"
            );
        }

        return step;
    }

    private void markTaskFailed(AgentPlanTask task, String errorMessage) {
        task.setStatus("FAILED");
        task.setErrorMessage(errorMessage);
        task.setUpdateTime(LocalDateTime.now());
        task.setEndTime(LocalDateTime.now());

        agentPlanTaskMapper.updateById(task);
    }

    private Map<String, Object> parseMap(String json) {
        if (!StringUtils.hasText(json) || "null".equals(json)) {
            return new LinkedHashMap<>();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<LinkedHashMap<String, Object>>() {
                    }
            );
        } catch (Exception e) {
            return new LinkedHashMap<>();
        }
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

    private AgentPlanTaskVO toTaskVO(AgentPlanTask task) {
        AgentPlanTaskVO vo = new AgentPlanTaskVO();

        vo.setId(task.getId());
        vo.setPlanTaskId(task.getPlanTaskId());
        vo.setProjectId(task.getProjectId());
        vo.setVersionNo(task.getVersionNo());
        vo.setModuleCode(task.getModuleCode());
        vo.setUserGoal(task.getUserGoal());
        vo.setPlanningMode(task.getPlanningMode());
        vo.setAllowedTools(task.getAllowedTools());
        vo.setRawModelOutput(task.getRawModelOutput());
        vo.setPlanJson(task.getPlanJson());
        vo.setStatus(task.getStatus());
        vo.setApproved(task.getApproved());
        vo.setCurrentStepIndex(task.getCurrentStepIndex());
        vo.setTotalStepCount(task.getTotalStepCount());
        vo.setSuccessStepCount(task.getSuccessStepCount());
        vo.setFailedStepCount(task.getFailedStepCount());
        vo.setFinalResult(task.getFinalResult());
        vo.setErrorMessage(task.getErrorMessage());
        vo.setCreateTime(task.getCreateTime());
        vo.setUpdateTime(task.getUpdateTime());
        vo.setStartTime(task.getStartTime());
        vo.setEndTime(task.getEndTime());

        return vo;
    }

    private AgentPlanStepVO toStepVO(AgentPlanStep step) {
        AgentPlanStepVO vo = new AgentPlanStepVO();

        vo.setId(step.getId());
        vo.setPlanTaskId(step.getPlanTaskId());
        vo.setStepIndex(step.getStepIndex());
        vo.setToolName(step.getToolName());
        vo.setStepName(step.getStepName());
        vo.setStepGoal(step.getStepGoal());
        vo.setInputParams(step.getInputParams());
        vo.setStatus(step.getStatus());
        vo.setOutputJson(step.getOutputJson());
        vo.setErrorMessage(step.getErrorMessage());
        vo.setRetryCount(step.getRetryCount());
        vo.setCreateTime(step.getCreateTime());
        vo.setUpdateTime(step.getUpdateTime());
        vo.setStartTime(step.getStartTime());
        vo.setEndTime(step.getEndTime());

        return vo;
    }
}