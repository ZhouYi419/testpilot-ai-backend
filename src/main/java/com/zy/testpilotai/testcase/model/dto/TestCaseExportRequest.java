package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseExportRequest {

    /**
     * 任务 ID。
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
     * 是否包含重复用例。
     * false：只导出 NORMAL 用例
     * true：导出全部用例
     */
    private Boolean includeDuplicate = false;
}