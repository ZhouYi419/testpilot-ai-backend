package com.zy.testpilotai.automation.model.dto;

import lombok.Data;

@Data
public class AutomationScriptQueryRequest {

    /**
     * 来源类型。
     */
    private String sourceType;

    /**
     * 用例集 ID。
     */
    private String caseSetId;

    /**
     * 测试用例任务 ID。
     */
    private String taskId;

    /**
     * 项目 ID。
     */
    private Long projectId;

    /**
     * 版本号。
     */
    private String versionNo;

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 状态。
     */
    private String status;
}