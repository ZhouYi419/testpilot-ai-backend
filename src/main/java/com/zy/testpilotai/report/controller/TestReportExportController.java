package com.zy.testpilotai.report.controller;

import com.zy.testpilotai.report.model.dto.AgentReportExcelExportRequest;
import com.zy.testpilotai.report.model.dto.TestCaseCompareExcelExportRequest;
import com.zy.testpilotai.report.model.dto.TestCaseSetExcelExportRequest;
import com.zy.testpilotai.report.service.TestReportExportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/report/export")
@RequiredArgsConstructor
public class TestReportExportController {

    private final TestReportExportService testReportExportService;

    /**
     * 导出用例集 Excel。
     */
    @PostMapping("/case-set")
    public void exportCaseSet(
            @RequestBody TestCaseSetExcelExportRequest request,
            HttpServletResponse response
    ) {
        testReportExportService.exportCaseSet(request, response);
    }

    /**
     * 导出用例集版本对比 Excel。
     */
    @PostMapping("/compare")
    public void exportCompare(
            @RequestBody TestCaseCompareExcelExportRequest request,
            HttpServletResponse response
    ) {
        testReportExportService.exportCompare(request, response);
    }

    /**
     * 导出 Agent 执行报告 Excel。
     */
    @PostMapping("/agent")
    public void exportAgentReport(
            @RequestBody AgentReportExcelExportRequest request,
            HttpServletResponse response
    ) {
        testReportExportService.exportAgentReport(request, response);
    }
}