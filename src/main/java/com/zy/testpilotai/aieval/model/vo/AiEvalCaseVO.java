package com.zy.testpilotai.aieval.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiEvalCaseVO {

    private Long id;

    private String caseId;

    private String datasetId;

    private String caseType;

    private String testDimension;

    private String caseName;

    private String inputText;

    private String contextText;

    private String expectedBehavior;

    private String expectedAnswer;

    private String expectedKeywords;

    private String forbiddenKeywords;

    private String expectedToolName;

    private String expectedSources;

    private String expectedOutputFormat;

    private String riskLevel;

    private String tags;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}