package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseVersionHistoryQueryRequest {

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
     * 用例集 ID。
     */
    private String caseSetId;

    /**
     * 测试用例 ID。
     */
    private Long testCaseId;

    /**
     * 对比任务 ID。
     */
    private String compareTaskId;

    /**
     * 快照类型。
     */
    private String snapshotType;
}