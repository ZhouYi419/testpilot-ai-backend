package com.zy.testpilotai.agent.planner.tool;

import lombok.Data;

@Data
public class AgentToolExecutionResult {

    /**
     * 是否成功。
     */
    private Boolean success;

    /**
     * 工具输出数据。
     */
    private Object data;

    /**
     * 消息。
     */
    private String message;

    public static AgentToolExecutionResult success(Object data) {
        AgentToolExecutionResult result = new AgentToolExecutionResult();
        result.setSuccess(true);
        result.setData(data);
        result.setMessage("执行成功");
        return result;
    }

    public static AgentToolExecutionResult failed(String message) {
        AgentToolExecutionResult result = new AgentToolExecutionResult();
        result.setSuccess(false);
        result.setMessage(message);
        return result;
    }
}