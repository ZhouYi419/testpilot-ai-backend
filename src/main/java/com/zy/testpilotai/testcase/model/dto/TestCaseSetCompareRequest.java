package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseSetCompareRequest {

    /**
     * 源用例集 ID。
     */
    private String sourceCaseSetId;

    /**
     * 目标用例集 ID。
     */
    private String targetCaseSetId;

    /**
     * 是否保存对比快照。
     */
    private Boolean snapshot;
}