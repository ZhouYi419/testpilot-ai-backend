package com.zy.testpilotai.agent.planner.model.dto;

import lombok.Data;
import java.util.LinkedHashMap;
import java.util.Map;

@Data
public class AiAgentPlanStepDTO {

    /**
     * 步骤名称。
     */
    private String stepName;

    /**
     * 工具名称。
     */
    private String toolName;

    /**
     * 步骤目标。
     */
    private String stepGoal;

    /**
     * 工具入参。
     */
    private Map<String, Object> inputParams = new LinkedHashMap<>();
}