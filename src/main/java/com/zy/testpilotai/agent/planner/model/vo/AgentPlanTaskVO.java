package com.zy.testpilotai.agent.planner.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AgentPlanTaskVO {

    private Long id;

    private String planTaskId;

    private Long projectId;

    private String versionNo;

    private String moduleCode;

    private String userGoal;

    private String planningMode;

    private String allowedTools;

    private String rawModelOutput;

    private String planJson;

    private String status;

    private Integer approved;

    private Integer currentStepIndex;

    private Integer totalStepCount;

    private Integer successStepCount;

    private Integer failedStepCount;

    private String finalResult;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}