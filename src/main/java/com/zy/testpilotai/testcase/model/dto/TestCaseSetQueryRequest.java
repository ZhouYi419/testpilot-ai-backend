package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseSetQueryRequest {

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
     * 用例集类型。
     */
    private String setType;

    /**
     * 状态。
     */
    private String status;

    /**
     * 关键词，匹配用例集名称 / 描述。
     */
    private String keyword;
}