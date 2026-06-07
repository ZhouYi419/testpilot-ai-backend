package com.zy.testpilotai.automation.model.dto;

import lombok.Data;

@Data
public class AutomationRunCancelRequest {

    /**
     * 自动化执行任务业务 ID。
     */
    private String runTaskId;
}