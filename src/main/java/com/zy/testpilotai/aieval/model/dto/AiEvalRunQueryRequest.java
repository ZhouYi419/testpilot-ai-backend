package com.zy.testpilotai.aieval.model.dto;

import lombok.Data;

@Data
public class AiEvalRunQueryRequest {

    private String datasetId;

    private String appConfigId;

    private Long projectId;

    private String versionNo;

    private String moduleCode;

    private String status;
}