package com.zy.testpilotai.agent.planner.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AgentPlanStepVO {

    private Long id;

    private String planTaskId;

    private Integer stepIndex;

    private String toolName;

    private String stepName;

    private String stepGoal;

    private String inputParams;

    private String status;

    private String outputJson;

    private String errorMessage;

    private Integer retryCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}