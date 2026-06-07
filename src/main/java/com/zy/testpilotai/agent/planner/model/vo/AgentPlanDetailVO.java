package com.zy.testpilotai.agent.planner.model.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AgentPlanDetailVO {

    /**
     * 计划任务。
     */
    private AgentPlanTaskVO task;

    /**
     * 计划步骤。
     */
    private List<AgentPlanStepVO> steps = new ArrayList<>();
}