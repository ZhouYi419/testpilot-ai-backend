package com.zy.testpilotai.agent.planner.model.dto;

import lombok.Data;

@Data
public class AgentPlanExecuteRequest {

    /**
     * Agent 计划任务业务 ID。
     */
    private String planTaskId;
}