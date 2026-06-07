package com.zy.testpilotai.automation.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AutomationRunTaskVO {

    private Long id;

    private String runTaskId;

    private String scriptTaskId;

    private Long projectId;

    private String versionNo;

    private String moduleCode;

    private String environmentName;

    private String executionMode;

    private String workDir;

    private String reportFilePath;

    private String baseUrl;

    private String extraEnv;

    private Integer timeoutSeconds;

    private Integer cancelRequested;

    private String status;

    private Integer exitCode;

    private Integer totalCount;

    private Integer passedCount;

    private Integer failedCount;

    private Integer errorCount;

    private Integer skippedCount;

    private Long durationMs;

    private String stdoutLog;

    private String stderrLog;

    private String junitXml;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}