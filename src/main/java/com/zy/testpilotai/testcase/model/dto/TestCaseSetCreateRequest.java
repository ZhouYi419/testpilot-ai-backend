package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseSetCreateRequest {

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