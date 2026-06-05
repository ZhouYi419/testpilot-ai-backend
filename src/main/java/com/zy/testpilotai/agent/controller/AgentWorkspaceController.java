package com.zy.testpilotai.agent.controller;

import com.zy.testpilotai.agent.model.dto.AgentRetryRequest;
import com.zy.testpilotai.agent.model.dto.AgentRunRequest;
import com.zy.testpilotai.agent.model.vo.AgentExecutionLogVO;
import com.zy.testpilotai.agent.model.vo.AgentRunResultVO;
import com.zy.testpilotai.agent.model.vo.AgentTaskStepVO;
import com.zy.testpilotai.agent.model.vo.AgentTaskVO;
import com.zy.testpilotai.agent.service.AgentWorkspaceService;
import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class AgentWorkspaceController {

    private final AgentWorkspaceService agentWorkspaceService;

    /**
     * 创建并异步运行 Agent 工作流。
     */
    @PostMapping("/run")
    public BaseResponse<AgentRunResultVO> run(
            @RequestBody @Valid AgentRunRequest request
    ) {
        return ResultUtils.success(agentWorkspaceService.run(request));
    }

    /**
     * 从指定步骤重试 Agent。
     */
    @PostMapping("/retry")
    public BaseResponse<AgentRunResultVO> retry(
            @RequestBody @Valid AgentRetryRequest request
    ) {
        return ResultUtils.success(agentWorkspaceService.retry(request));
    }

    /**
     * 取消 Agent 任务。
     */
    @PostMapping("/task/{agentTaskId}/cancel")
    public BaseResponse<Boolean> cancel(
            @PathVariable String agentTaskId
    ) {
        return ResultUtils.success(agentWorkspaceService.cancel(agentTaskId));
    }

    /**
     * 查询 Agent 任务详情。
     */
    @GetMapping("/task/{agentTaskId}")
    public BaseResponse<AgentTaskVO> getTask(
            @PathVariable String agentTaskId
    ) {
        return ResultUtils.success(agentWorkspaceService.getTask(agentTaskId));
    }

    /**
     * 查询 Agent 执行步骤。
     */
    @GetMapping("/task/{agentTaskId}/steps")
    public BaseResponse<List<AgentTaskStepVO>> listSteps(
            @PathVariable String agentTaskId
    ) {
        return ResultUtils.success(agentWorkspaceService.listSteps(agentTaskId));
    }

    /**
     * 查询 Agent 执行日志。
     */
    @GetMapping("/task/{agentTaskId}/logs")
    public BaseResponse<List<AgentExecutionLogVO>> listLogs(
            @PathVariable String agentTaskId
    ) {
        return ResultUtils.success(agentWorkspaceService.listLogs(agentTaskId));
    }
}