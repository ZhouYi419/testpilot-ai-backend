package com.zy.testpilotai.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.agent.mapper.AgentTaskMapper;
import com.zy.testpilotai.agent.mapper.AgentTaskStepMapper;
import com.zy.testpilotai.agent.model.dto.AgentRetryRequest;
import com.zy.testpilotai.agent.model.dto.AgentRunRequest;
import com.zy.testpilotai.agent.model.entity.AgentTask;
import com.zy.testpilotai.agent.model.entity.AgentTaskStep;
import com.zy.testpilotai.agent.model.vo.AgentExecutionLogVO;
import com.zy.testpilotai.agent.model.vo.AgentRunResultVO;
import com.zy.testpilotai.agent.model.vo.AgentTaskStepVO;
import com.zy.testpilotai.agent.model.vo.AgentTaskVO;
import com.zy.testpilotai.agent.service.AgentExecutionLogService;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AgentWorkspaceServiceImpl implements com.zy.testpilotai.agent.service.AgentWorkspaceService {

    private final AgentTaskMapper agentTaskMapper;

    private final AgentTaskStepMapper agentTaskStepMapper;

    private final AgentAsyncProcessor agentAsyncProcessor;

    private final ObjectMapper objectMapper;

    private final AgentExecutionLogService agentExecutionLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentRunResultVO run(AgentRunRequest request) {
        validateRequest(request);

        String agentTaskId = "agent_" + UUID.randomUUID().toString().replace("-", "");

        AgentTask task = createAgentTask(agentTaskId, request);

        agentAsyncProcessor.execute(agentTaskId);

        return toRunResultVO(task);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AgentRunResultVO retry(AgentRetryRequest request) {
        AgentTask task = getAgentTask(request.getAgentTaskId());

        if ("RUNNING".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Agent 正在执行中，不能重试");
        }

        if (request.getStartStepIndex() == null || request.getStartStepIndex() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "重试起始步骤必须大于 0");
        }

        /*
         * 清理重试起点之后的步骤记录。
         * 这样前端看到的是新一轮执行记录。
         */
        agentTaskStepMapper.delete(
                new LambdaQueryWrapper<AgentTaskStep>()
                        .eq(AgentTaskStep::getAgentTaskId, request.getAgentTaskId())
                        .ge(AgentTaskStep::getStepIndex, request.getStartStepIndex())
        );

        /*
         * 如果从第一步重试，需要清空下游关联结果。
         * 如果从第二步重试，保留第一步产生的 analysisTaskId 等结果。
         */
        if (request.getStartStepIndex() <= 1) {
            task.setAnalysisTaskId(null);
            task.setTestcaseTaskId(null);
            task.setAiAppTaskId(null);
            task.setFinalResult(null);
        } else if (request.getStartStepIndex() <= 2) {
            task.setTestcaseTaskId(null);
            task.setAiAppTaskId(null);
            task.setFinalResult(null);
        } else {
            task.setFinalResult(null);
        }

        task.setStatus("PENDING");
        task.setCancelRequested(0);
        task.setResumeFromStep(request.getStartStepIndex());
        task.setRetryCount(task.getRetryCount() == null ? 1 : task.getRetryCount() + 1);
        task.setErrorMessage(null);
        task.setEndTime(null);
        task.setUpdateTime(LocalDateTime.now());

        agentTaskMapper.updateById(task);

        agentAsyncProcessor.execute(task.getAgentTaskId());

        return toRunResultVO(task);
    }

    @Override
    public Boolean cancel(String agentTaskId) {
        AgentTask task = getAgentTask(agentTaskId);

        if ("SUCCESS".equals(task.getStatus())
                || "FAILED".equals(task.getStatus())
                || "CANCELLED".equals(task.getStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前任务已结束，不能取消");
        }

        task.setCancelRequested(1);
        task.setStatus("CANCELLING");
        task.setUpdateTime(LocalDateTime.now());
        agentTaskMapper.updateById(task);

        return true;
    }

    @Override
    public AgentTaskVO getTask(String agentTaskId) {
        AgentTask task = getAgentTask(agentTaskId);

        AgentTaskVO vo = toTaskVO(task);
        vo.setSteps(listSteps(agentTaskId));

        return vo;
    }

    @Override
    public List<AgentTaskStepVO> listSteps(String agentTaskId) {
        return agentTaskStepMapper.selectList(
                        new LambdaQueryWrapper<AgentTaskStep>()
                                .eq(AgentTaskStep::getAgentTaskId, agentTaskId)
                                .orderByAsc(AgentTaskStep::getStepIndex)
                                .orderByAsc(AgentTaskStep::getId)
                )
                .stream()
                .map(this::toStepVO)
                .toList();
    }

    @Override
    public List<AgentExecutionLogVO> listLogs(String agentTaskId) {
        // 先校验任务存在
        getAgentTask(agentTaskId);

        return agentExecutionLogService.listByAgentTaskId(agentTaskId);
    }

    private AgentTask createAgentTask(String agentTaskId, AgentRunRequest request) {
        AgentTask task = new AgentTask();

        task.setAgentTaskId(agentTaskId);
        task.setWorkflowType(request.getWorkflowType());
        task.setProjectId(request.getProjectId());
        task.setBaseVersionNo(request.getBaseVersionNo());
        task.setTargetVersionNo(request.getTargetVersionNo());
        task.setModuleCode(request.getModuleCode());
        task.setUserGoal(request.getUserGoal());
        task.setNewRequirement(request.getNewRequirement());
        task.setAppType(request.getAppType());
        task.setAppDescription(request.getAppDescription());
        task.setSelectedSkills(toJson(request.getSelectedSkills()));

        // 异步任务初始状态为 PENDING，后台开始后会改为 RUNNING
        task.setStatus("PENDING");
        task.setExecutionMode("ASYNC");
        task.setCurrentStepIndex(null);
        task.setCancelRequested(0);
        task.setRetryCount(0);
        task.setResumeFromStep(1);

        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        agentTaskMapper.insert(task);

        return task;
    }

    private void validateRequest(AgentRunRequest request) {
        if (!StringUtils.hasText(request.getWorkflowType())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "工作流类型不能为空");
        }

        if (!StringUtils.hasText(request.getUserGoal())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户目标不能为空");
        }

        if ("STANDARD_TEST_DESIGN".equals(request.getWorkflowType())) {
            if (request.getProjectId() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "普通测试设计 Agent 必须传 projectId");
            }
            if (!StringUtils.hasText(request.getTargetVersionNo())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "普通测试设计 Agent 必须传 targetVersionNo 作为版本号");
            }
        }

        if ("INCREMENTAL_TEST_DESIGN".equals(request.getWorkflowType())) {
            if (request.getProjectId() == null) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "增量测试 Agent 必须传 projectId");
            }
            if (!StringUtils.hasText(request.getBaseVersionNo())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "增量测试 Agent 必须传 baseVersionNo");
            }
            if (!StringUtils.hasText(request.getTargetVersionNo())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "增量测试 Agent 必须传 targetVersionNo");
            }
            if (!StringUtils.hasText(request.getNewRequirement())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "增量测试 Agent 必须传 newRequirement");
            }
        }

        if ("AI_APP_TEST_DESIGN".equals(request.getWorkflowType())) {
            if (!StringUtils.hasText(request.getAppType())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "AI 应用测试 Agent 必须传 appType");
            }
            if (!StringUtils.hasText(request.getAppDescription())) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "AI 应用测试 Agent 必须传 appDescription");
            }
        }
    }

    private AgentTask getAgentTask(String agentTaskId) {
        AgentTask task = agentTaskMapper.selectOne(
                new LambdaQueryWrapper<AgentTask>()
                        .eq(AgentTask::getAgentTaskId, agentTaskId)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Agent 任务不存在");
        }

        return task;
    }

    private String toJson(Object object) {
        try {
            if (object == null) {
                return "null";
            }
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "JSON 序列化失败：" + e.getMessage());
        }
    }

    private AgentRunResultVO toRunResultVO(AgentTask task) {
        AgentRunResultVO vo = new AgentRunResultVO();

        vo.setAgentTaskId(task.getAgentTaskId());
        vo.setWorkflowType(task.getWorkflowType());
        vo.setStatus(task.getStatus());
        vo.setAnalysisTaskId(task.getAnalysisTaskId());
        vo.setTestcaseTaskId(task.getTestcaseTaskId());
        vo.setAiAppTaskId(task.getAiAppTaskId());
        vo.setFinalResult(task.getFinalResult());
        vo.setErrorMessage(task.getErrorMessage());

        return vo;
    }

    private AgentTaskVO toTaskVO(AgentTask task) {
        AgentTaskVO vo = new AgentTaskVO();

        vo.setAgentTaskId(task.getAgentTaskId());
        vo.setWorkflowType(task.getWorkflowType());
        vo.setProjectId(task.getProjectId());
        vo.setBaseVersionNo(task.getBaseVersionNo());
        vo.setTargetVersionNo(task.getTargetVersionNo());
        vo.setModuleCode(task.getModuleCode());
        vo.setUserGoal(task.getUserGoal());
        vo.setNewRequirement(task.getNewRequirement());
        vo.setAppType(task.getAppType());
        vo.setAppDescription(task.getAppDescription());
        vo.setSelectedSkills(task.getSelectedSkills());
        vo.setStatus(task.getStatus());
        vo.setFinalResult(task.getFinalResult());
        vo.setAnalysisTaskId(task.getAnalysisTaskId());
        vo.setTestcaseTaskId(task.getTestcaseTaskId());
        vo.setAiAppTaskId(task.getAiAppTaskId());
        vo.setErrorMessage(task.getErrorMessage());
        vo.setCreateTime(task.getCreateTime());
        vo.setUpdateTime(task.getUpdateTime());

        // 异步执行增强字段
        vo.setExecutionMode(task.getExecutionMode());
        vo.setCurrentStepIndex(task.getCurrentStepIndex());
        vo.setCancelRequested(task.getCancelRequested());
        vo.setStartTime(task.getStartTime());
        vo.setEndTime(task.getEndTime());
        vo.setRetryCount(task.getRetryCount());
        vo.setResumeFromStep(task.getResumeFromStep());

        return vo;
    }

    private AgentTaskStepVO toStepVO(AgentTaskStep step) {
        AgentTaskStepVO vo = new AgentTaskStepVO();

        vo.setId(step.getId());
        vo.setAgentTaskId(step.getAgentTaskId());
        vo.setStepIndex(step.getStepIndex());
        vo.setStepName(step.getStepName());
        vo.setStatus(step.getStatus());
        vo.setInput(step.getInput());
        vo.setOutput(step.getOutput());
        vo.setErrorMessage(step.getErrorMessage());
        vo.setStartTime(step.getStartTime());
        vo.setEndTime(step.getEndTime());

        // 异步执行增强字段
        vo.setRetryCount(step.getRetryCount());
        vo.setRetryable(step.getRetryable());
        vo.setStepType(step.getStepType());

        return vo;
    }
}