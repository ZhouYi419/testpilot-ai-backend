package com.zy.testpilotai.aieval.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiEvalResultVO {

    private Long id;

    private String runId;

    private String caseId;

    private String datasetId;

    private String appConfigId;

    private String caseType;

    private String testDimension;

    private String caseName;

    private String inputText;

    private String requestPayload;

    private Integer httpStatus;

    private String responseBody;

    private String modelOutput;

    private Integer passed;

    private Integer accuracyPass;

    private Integer securityPass;

    private Integer formatPass;

    private Integer toolCallPass;

    private Integer sourcePass;

    private Integer expectedKeywordHit;

    private Integer forbiddenKeywordHit;

    private String matchedExpectedKeywords;

    private String matchedForbiddenKeywords;

    private Double score;

    private Long latencyMs;

    private String evaluationMessage;

    private String errorMessage;

    private LocalDateTime createTime;
}