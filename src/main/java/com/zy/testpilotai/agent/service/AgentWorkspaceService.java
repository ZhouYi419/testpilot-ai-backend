package com.zy.testpilotai.agent.service;

import com.zy.testpilotai.agent.model.dto.AgentRetryRequest;
import com.zy.testpilotai.agent.model.dto.AgentRunRequest;
import com.zy.testpilotai.agent.model.vo.AgentExecutionLogVO;
import com.zy.testpilotai.agent.model.vo.AgentRunResultVO;
import com.zy.testpilotai.agent.model.vo.AgentTaskStepVO;
import com.zy.testpilotai.agent.model.vo.AgentTaskVO;
import java.util.List;

public interface AgentWorkspaceService {

    /**
     * 创建并异步运行 Agent 工作流。
     */
    AgentRunResultVO run(AgentRunRequest request);

    /**
     * 从指定步骤重试 Agent。
     */
    AgentRunResultVO retry(AgentRetryRequest request);

    /**
     * 取消 Agent 任务。
     */
    Boolean cancel(String agentTaskId);

    /**
     * 查询 Agent 任务详情。
     */
    AgentTaskVO getTask(String agentTaskId);

    /**
     * 查询 Agent 执行步骤。
     */
    List<AgentTaskStepVO> listSteps(String agentTaskId);

    /**
     * 查询 Agent 执行日志。
     */
    List<AgentExecutionLogVO> listLogs(String agentTaskId);
}