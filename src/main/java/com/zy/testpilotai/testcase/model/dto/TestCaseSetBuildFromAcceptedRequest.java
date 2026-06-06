package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseSetBuildFromAcceptedRequest {

    /**
     * 项目 ID。
     */
    private Long projectId;

    /**
     * 版本号。
     */
    private String versionNo;

    /**
     * 模块编码，可为空。
     */
    private String moduleCode;

    /**
     * 任务 ID，可为空。
     * 如果传了 taskId，则只从该任务下的已采纳用例构建用例集。
     */
    private String taskId;

    /**
     * 用例集名称。
     */
    private String setName;

    /**
     * 用例集类型：
     * FULL / INCREMENTAL / REGRESSION / AI_APP / CUSTOM。
     */
    private String setType;

    /**
     * 描述。
     */
    private String description;
}