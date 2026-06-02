package com.zy.testpilotai.testcase.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestcaseGenerationTaskEntity {

    private Long id;

    private Long projectId;

    private String versionName;

    private String moduleName;

    private String requirementText;

    /**
     * PENDING / RUNNING / SUCCESS / FAILED
     */
    private String status;

    private Integer totalCases;

    private String modelName;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}