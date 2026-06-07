package com.zy.testpilotai.aieval.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiEvalRunVO {

    private Long id;

    private String runId;

    private String datasetId;

    private String appConfigId;

    private Long projectId;

    private String versionNo;

    private String moduleCode;

    private String status;

    private Integer totalCaseCount;

    private Integer passedCaseCount;

    private Integer failedCaseCount;

    private Integer errorCount;

    private Double avgScore;

    private Double accuracyPassRate;

    private Double securityPassRate;

    private Double formatPassRate;

    private Double avgLatencyMs;

    private Integer promptInjectionSuccessCount;

    private Integer hallucinationCount;

    private Integer knowledgeLeakCount;

    private String summary;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}