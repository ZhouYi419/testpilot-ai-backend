package com.zy.testpilotai.testcase.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.testcase.model.dto.TestCaseManualQueryRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseReviewStatusRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseUpdateRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseManualVO;
import com.zy.testpilotai.testcase.service.TestCaseManualReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testcase/manual")
@RequiredArgsConstructor
public class TestCaseManualReviewController {

    private final TestCaseManualReviewService testCaseManualReviewService;

    /**
     * 查询人工管理用例列表。
     */
    @PostMapping("/list")
    public BaseResponse<List<TestCaseManualVO>> list(
            @RequestBody TestCaseManualQueryRequest request
    ) {
        return ResultUtils.success(testCaseManualReviewService.list(request));
    }

    /**
     * 人工编辑测试用例。
     */
    @PostMapping("/update")
    public BaseResponse<TestCaseManualVO> update(
            @RequestBody TestCaseUpdateRequest request
    ) {
        return ResultUtils.success(testCaseManualReviewService.update(request));
    }

    /**
     * 采纳测试用例。
     */
    @PostMapping("/accept")
    public BaseResponse<Boolean> accept(
            @RequestBody TestCaseReviewStatusRequest request
    ) {
        return ResultUtils.success(testCaseManualReviewService.accept(request));
    }

    /**
     * 驳回测试用例。
     */
    @PostMapping("/reject")
    public BaseResponse<Boolean> reject(
            @RequestBody TestCaseReviewStatusRequest request
    ) {
        return ResultUtils.success(testCaseManualReviewService.reject(request));
    }

    /**
     * 删除测试用例。
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> delete(
            @RequestBody TestCaseReviewStatusRequest request
    ) {
        return ResultUtils.success(testCaseManualReviewService.delete(request));
    }
}