package com.zy.testpilotai.testcase.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.testcase.model.dto.TestCaseGenerateRequest;
import com.zy.testpilotai.testcase.model.dto.TestCasePageRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseGenerateResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseVO;
import com.zy.testpilotai.testcase.service.TestCaseGenerateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/testcase")
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseGenerateService testCaseGenerateService;

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
}