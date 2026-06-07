package com.zy.testpilotai.agent.planner.model.dto;

import lombok.Data;

@Data
public class AgentPlanRetryStepRequest {

    /**
     * Agent 计划任务业务 ID。
     */
    private String planTaskId;

    /**
     * 步骤序号。
     */
    private Integer stepIndex;
}