package com.zy.testpilotai.report.service;

import com.zy.testpilotai.report.model.dto.AgentReportExcelExportRequest;
import com.zy.testpilotai.report.model.dto.TestCaseCompareExcelExportRequest;
import com.zy.testpilotai.report.model.dto.TestCaseSetExcelExportRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface TestReportExportService {

    /**
     * 导出用例集 Excel。
     */
    void exportCaseSet(
            TestCaseSetExcelExportRequest request,
            HttpServletResponse response
    );

    /**
     * 导出用例集版本对比 Excel。
     */
    void exportCompare(
            TestCaseCompareExcelExportRequest request,
            HttpServletResponse response
    );

    /**
     * 导出 Agent 执行报告 Excel。
     */
    void exportAgentReport(
            AgentReportExcelExportRequest request,
            HttpServletResponse response
    );
}