package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseSetCompareQueryRequest {

    /**
     * 项目 ID。
     */
    private Long projectId;

    /**
     * 源用例集 ID。
     */
    private String sourceCaseSetId;

    /**
     * 目标用例集 ID。
     */
    private String targetCaseSetId;

    /**
     * 状态。
     */
    private String status;
}