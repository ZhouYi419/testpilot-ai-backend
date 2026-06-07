package com.zy.testpilotai.automation.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AutomationCaseResultVO {

    private Long id;

    private String runTaskId;

    private String className;

    private String caseName;

    private String status;

    private Double timeSeconds;

    private String message;

    private String detail;

    private String systemOut;

    private String systemErr;

    private LocalDateTime createTime;
}