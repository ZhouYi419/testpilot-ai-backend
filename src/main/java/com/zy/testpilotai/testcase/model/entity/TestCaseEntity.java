package com.zy.testpilotai.testcase.model.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TestCaseEntity {

    private Long id;

    private Long projectId;

    private Long taskId;

    private String versionName;

    private String moduleName;

    private String caseTitle;

    private String priority;

    private String caseType;

    private String precondition;

    /**
     * 用换行保存步骤。
     */
    private String steps;

    private String expectedResult;

    private String testData;

    /**
     * 逗号分隔的 chunkId，例如：1,2,3
     */
    private String sourceChunkIds;

    private BigDecimal aiScore;

    /**
     * UNREVIEWED / APPROVED / REJECTED
     */
    private String reviewStatus;

    private Boolean deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}