package com.zy.testpilotai.testcase.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetCompareQueryRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetCompareRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetSnapshotRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseVersionHistoryQueryRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseSetCompareDetailVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseSetCompareTaskVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseVersionHistoryVO;
import com.zy.testpilotai.testcase.service.TestCaseVersionCompareService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/testcase-version")
@RequiredArgsConstructor
public class TestCaseVersionCompareController {

    private final TestCaseVersionCompareService testCaseVersionCompareService;

    /**
     * 手动生成用例集快照。
     */
    @PostMapping("/snapshot")
    public BaseResponse<List<TestCaseVersionHistoryVO>> snapshot(
            @RequestBody TestCaseSetSnapshotRequest request
    ) {
        return ResultUtils.success(testCaseVersionCompareService.snapshot(request));
    }

    /**
     * 对比两个用例集。
     */
    @PostMapping("/compare")
    public BaseResponse<TestCaseSetCompareDetailVO> compare(
            @RequestBody TestCaseSetCompareRequest request
    ) {
        return ResultUtils.success(testCaseVersionCompareService.compare(request));
    }

    /**
     * 查询对比任务列表。
     */
    @PostMapping("/compare/list")
    public BaseResponse<List<TestCaseSetCompareTaskVO>> listCompareTasks(
            @RequestBody TestCaseSetCompareQueryRequest request
    ) {
        return ResultUtils.success(testCaseVersionCompareService.listCompareTasks(request));
    }

    /**
     * 查询对比详情。
     */
    @GetMapping("/compare/detail/{compareTaskId}")
    public BaseResponse<TestCaseSetCompareDetailVO> compareDetail(
            @PathVariable String compareTaskId
    ) {
        return ResultUtils.success(testCaseVersionCompareService.compareDetail(compareTaskId));
    }

    /**
     * 查询用例版本历史。
     */
    @PostMapping("/history/list")
    public BaseResponse<List<TestCaseVersionHistoryVO>> listHistory(
            @RequestBody TestCaseVersionHistoryQueryRequest request
    ) {
        return ResultUtils.success(testCaseVersionCompareService.listHistory(request));
    }
}