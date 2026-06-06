package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseManualQueryRequest {

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
     * 人工审核状态。
     */
    private String reviewStatus;

    /**
     * 关键词，匹配用例标题 / 模块名称。
     */
    private String keyword;
}