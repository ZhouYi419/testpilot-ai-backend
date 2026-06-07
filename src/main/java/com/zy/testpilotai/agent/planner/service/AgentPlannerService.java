package com.zy.testpilotai.agent.planner.service;

import com.zy.testpilotai.agent.planner.model.dto.AgentPlanCreateRequest;
import com.zy.testpilotai.agent.planner.model.dto.AgentPlanExecuteRequest;
import com.zy.testpilotai.agent.planner.model.dto.AgentPlanQueryRequest;
import com.zy.testpilotai.agent.planner.model.dto.AgentPlanRetryStepRequest;
import com.zy.testpilotai.agent.planner.model.vo.AgentPlanDetailVO;
import com.zy.testpilotai.agent.planner.model.vo.AgentPlanTaskVO;
import com.zy.testpilotai.agent.planner.model.vo.AgentToolInfoVO;

import java.util.List;

public interface AgentPlannerService {

    /**
     * 查询可用工具列表。
     */
    List<AgentToolInfoVO> listTools();

    /**
     * 创建 Agent 执行计划。
     */
    AgentPlanDetailVO createPlan(AgentPlanCreateRequest request);

    /**
     * 确认并执行计划。
     */
    AgentPlanDetailVO approveAndExecute(AgentPlanExecuteRequest request);

    /**
     * 查询计划列表。
     */
    List<AgentPlanTaskVO> list(AgentPlanQueryRequest request);

    /**
     * 查询计划详情。
     */
    AgentPlanDetailVO detail(String planTaskId);

    /**
     * 重试指定步骤。
     */
    AgentPlanDetailVO retryStep(AgentPlanRetryStepRequest request);
}