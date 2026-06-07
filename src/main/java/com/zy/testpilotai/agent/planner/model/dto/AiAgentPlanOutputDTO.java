package com.zy.testpilotai.agent.planner.model.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiAgentPlanOutputDTO {

    /**
     * 计划名称。
     */
    private String planName;

    /**
     * 计划说明。
     */
    private String planDescription;

    /**
     * 步骤列表。
     */
    private List<AiAgentPlanStepDTO> steps = new ArrayList<>();
}