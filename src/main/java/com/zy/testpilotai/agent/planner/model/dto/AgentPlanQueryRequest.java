package com.zy.testpilotai.agent.planner.model.dto;

import lombok.Data;

@Data
public class AgentPlanQueryRequest {

    private Long projectId;

    private String versionNo;

    private String moduleCode;

    private String status;

    private String keyword;
}