package com.zy.testpilotai.testcase.service;

import com.zy.testpilotai.testcase.model.dto.TestCaseDeduplicateRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseExportRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseDeduplicateResultVO;
import jakarta.servlet.http.HttpServletResponse;

public interface TestCaseToolService {

    /**
     * 对测试用例进行去重。
     */
    TestCaseDeduplicateResultVO deduplicate(TestCaseDeduplicateRequest request);

    /**
     * 导出测试用例 Excel。
     */
    void exportExcel(TestCaseExportRequest request, HttpServletResponse response);
}