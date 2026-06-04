package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCasePageRequest {

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 版本号
     */
    private String versionNo;

    /**
     * 模块编码
     */
    private String moduleCode;
}