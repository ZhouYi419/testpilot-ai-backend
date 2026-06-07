package com.zy.testpilotai.agent.planner.tool;

import java.util.List;
import java.util.Map;

public interface AgentToolExecutor {

    /**
     * 工具名称。
     */
    String toolName();

    /**
     * 工具描述。
     */
    String description();

    /**
     * 必填参数名称。
     */
    List<String> requiredParams();

    /**
     * 执行工具。
     */
    AgentToolExecutionResult execute(Map<String, Object> inputParams);
}