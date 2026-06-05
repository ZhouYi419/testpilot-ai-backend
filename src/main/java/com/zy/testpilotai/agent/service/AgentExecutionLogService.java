package com.zy.testpilotai.agent.service;

import com.zy.testpilotai.agent.model.vo.AgentExecutionLogVO;
import java.util.List;

public interface AgentExecutionLogService {

    /**
     * 记录普通日志。
     */
    void info(
            String agentTaskId,
            Integer stepIndex,
            String stepType,
            String stepName,
            String eventType,
            String message,
            Object input,
            Object output,
            Long durationMs
    );

    /**
     * 记录警告日志。
     */
    void warn(
            String agentTaskId,
            Integer stepIndex,
            String stepType,
            String stepName,
            String eventType,
            String message,
            Object input,
            Object output,
            Long durationMs
    );

    /**
     * 记录错误日志。
     */
    void error(
            String agentTaskId,
            Integer stepIndex,
            String stepType,
            String stepName,
            String eventType,
            String message,
            Object input,
            Throwable throwable,
            Long durationMs
    );

    /**
     * 查询某个 Agent 任务的执行日志。
     */
    List<AgentExecutionLogVO> listByAgentTaskId(String agentTaskId);
}