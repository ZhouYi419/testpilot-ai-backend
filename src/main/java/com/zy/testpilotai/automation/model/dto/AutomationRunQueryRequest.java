package com.zy.testpilotai.automation.model.dto;

import lombok.Data;

@Data
public class AutomationRunQueryRequest {

    /**
     * 脚本生成任务业务 ID。
     */
    private String scriptTaskId;

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
     * 执行环境名称。
     */
    private String environmentName;

    /**
     * 状态。
     */
    private String status;
}