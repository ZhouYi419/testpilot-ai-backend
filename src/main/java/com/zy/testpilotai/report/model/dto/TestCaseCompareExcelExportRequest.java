package com.zy.testpilotai.report.model.dto;

import lombok.Data;

@Data
public class TestCaseCompareExcelExportRequest {

    /**
     * 用例集对比任务 ID。
     */
    private String compareTaskId;
}