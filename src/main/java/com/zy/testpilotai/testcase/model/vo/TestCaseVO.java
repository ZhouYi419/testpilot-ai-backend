package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TestCaseVO {

    private Long id;

    private Long projectId;

    private Long taskId;

    private String versionName;

    private String moduleName;

    private String caseTitle;

    private String priority;

    private String caseType;

    private String precondition;

    private String steps;

    private String expectedResult;

    private String testData;

    private String sourceChunkIds;

    private BigDecimal aiScore;

    private String reviewStatus;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}