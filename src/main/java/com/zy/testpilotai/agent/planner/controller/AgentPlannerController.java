package com.zy.testpilotai.agent.planner.controller;

import com.zy.testpilotai.agent.planner.model.dto.AgentPlanCreateRequest;
import com.zy.testpilotai.agent.planner.model.dto.AgentPlanExecuteRequest;
import com.zy.testpilotai.agent.planner.model.dto.AgentPlanQueryRequest;
import com.zy.testpilotai.agent.planner.model.dto.AgentPlanRetryStepRequest;
import com.zy.testpilotai.agent.planner.model.vo.AgentPlanDetailVO;
import com.zy.testpilotai.agent.planner.model.vo.AgentPlanTaskVO;
import com.zy.testpilotai.agent.planner.model.vo.AgentToolInfoVO;
import com.zy.testpilotai.agent.planner.service.AgentPlannerService;
import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/agent/planner")
@RequiredArgsConstructor
public class AgentPlannerController {

    private final AgentPlannerService agentPlannerService;

    /**
     * 查询 Agent 可用工具。
     */
    @GetMapping("/tools")
    public BaseResponse<List<AgentToolInfoVO>> listTools() {
        return ResultUtils.success(agentPlannerService.listTools());
    }

    /**
     * 创建 Agent 执行计划。
     */
    @PostMapping("/plan/create")
    public BaseResponse<AgentPlanDetailVO> createPlan(
            @RequestBody AgentPlanCreateRequest request
    ) {
        return ResultUtils.success(agentPlannerService.createPlan(request));
    }

    /**
     * 确认并执行计划。
     */
    @PostMapping("/plan/execute")
    public BaseResponse<AgentPlanDetailVO> approveAndExecute(
            @RequestBody AgentPlanExecuteRequest request
    ) {
        return ResultUtils.success(agentPlannerService.approveAndExecute(request));
    }

    /**
     * 查询计划列表。
     */
    @PostMapping("/plan/list")
    public BaseResponse<List<AgentPlanTaskVO>> list(
            @RequestBody AgentPlanQueryRequest request
    ) {
        return ResultUtils.success(agentPlannerService.list(request));
    }

    /**
     * 查询计划详情。
     */
    @GetMapping("/plan/detail/{planTaskId}")
    public BaseResponse<AgentPlanDetailVO> detail(
            @PathVariable String planTaskId
    ) {
        return ResultUtils.success(agentPlannerService.detail(planTaskId));
    }

    /**
     * 重试失败步骤。
     */
    @PostMapping("/plan/retry-step")
    public BaseResponse<AgentPlanDetailVO> retryStep(
            @RequestBody AgentPlanRetryStepRequest request
    ) {
        return ResultUtils.success(agentPlannerService.retryStep(request));
    }
}