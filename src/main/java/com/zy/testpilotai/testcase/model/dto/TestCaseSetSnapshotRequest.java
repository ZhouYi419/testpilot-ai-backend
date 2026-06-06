package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseSetSnapshotRequest {

    /**
     * 用例集 ID。
     */
    private String caseSetId;

    /**
     * 快照类型。
     * 可不传，默认 MANUAL_SNAPSHOT。
     */
    private String snapshotType;
}