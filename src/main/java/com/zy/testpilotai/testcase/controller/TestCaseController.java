package com.zy.testpilotai.testcase.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.testcase.model.dto.MissingCaseCompleteRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseDeduplicateRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseExportRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseGenerateRequest;
import com.zy.testpilotai.testcase.model.dto.TestCasePageRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseReviewRequest;
import com.zy.testpilotai.testcase.model.vo.MissingCaseCompleteResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseDeduplicateResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseGenerateResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseQualityReviewResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseVO;
import com.zy.testpilotai.testcase.service.TestCaseGenerateService;
import com.zy.testpilotai.testcase.service.TestCaseQualityService;
import com.zy.testpilotai.testcase.service.TestCaseToolService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testcase")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseGenerateService testCaseGenerateService;

    private final TestCaseQualityService testCaseQualityService;

    private final TestCaseToolService testCaseToolService;

    /**
     * 基于知识库生成测试用例。
     */
    @PostMapping("/generate")
    public BaseResponse<TestCaseGenerateResultVO> generate(
            @RequestBody @Valid TestCaseGenerateRequest request
    ) {
        return ResultUtils.success(testCaseGenerateService.generate(request));
    }

    /**
     * 查询测试用例列表。
     */
    @PostMapping("/list")
    public BaseResponse<List<TestCaseVO>> list(
            @RequestBody TestCasePageRequest request
    ) {
        return ResultUtils.success(testCaseGenerateService.list(request));
    }

    /**
     * 对某个生成任务下的测试用例进行质量评审。
     */
    @PostMapping("/review")
    public BaseResponse<TestCaseQualityReviewResultVO> review(
            @RequestBody @Valid TestCaseReviewRequest request
    ) {
        return ResultUtils.success(testCaseQualityService.review(request));
    }

    /**
     * 根据质量评审结果补全缺失测试用例。
     */
    @PostMapping("/complete-missing")
    public BaseResponse<MissingCaseCompleteResultVO> completeMissing(
            @RequestBody @Valid MissingCaseCompleteRequest request
    ) {
        return ResultUtils.success(testCaseQualityService.completeMissing(request));
    }

    /**
     * 对测试用例做去重。
     */
    @PostMapping("/deduplicate")
    public BaseResponse<TestCaseDeduplicateResultVO> deduplicate(
            @RequestBody TestCaseDeduplicateRequest request
    ) {
        return ResultUtils.success(testCaseToolService.deduplicate(request));
    }

    /**
     * 导出测试用例 Excel
     */
    @PostMapping("/export")
    public void export(
            @RequestBody TestCaseExportRequest request,
            HttpServletResponse response
    ) {
        testCaseToolService.exportExcel(request, response);
    }
}