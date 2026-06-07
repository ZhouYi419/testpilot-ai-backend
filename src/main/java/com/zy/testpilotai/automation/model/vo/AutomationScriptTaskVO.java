package com.zy.testpilotai.automation.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AutomationScriptTaskVO {

    private Long id;

    private String scriptTaskId;

    private String sourceType;

    private String caseSetId;

    private String testcaseTaskId;

    private Long projectId;

    private String versionNo;

    private String moduleCode;

    private String scriptFramework;

    private String generateMode;

    private String baseUrl;

    private String authType;

    private String authHeaderName;

    private String tokenPlaceholder;

    private String commonHeaders;

    private String selectedCaseIds;

    private String status;

    private Integer caseCount;

    private Integer fileCount;

    private String rawModelOutput;

    private String errorMessage;

    private String summary;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}