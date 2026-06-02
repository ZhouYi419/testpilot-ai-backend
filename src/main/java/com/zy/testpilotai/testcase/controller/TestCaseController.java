package com.zy.testpilotai.testcase.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.testcase.model.dto.TestCaseGenerateRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseGenerateResponseVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseVO;
import com.zy.testpilotai.testcase.service.TestCaseGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class TestCaseController {

    private final TestCaseGenerationService testCaseGenerationService;

    /**
     * 基于知识库生成测试用例
     */
    @PostMapping("/api/testcases/generate")
    public BaseResponse<TestCaseGenerateResponseVO> generate(@Valid @RequestBody TestCaseGenerateRequest request) {
        return ResultUtils.success(
                testCaseGenerationService.generate(request)
        );
    }

    /**
     * 查询测试用例列表
     */
    @GetMapping("/api/testcases")
    public BaseResponse<List<TestCaseVO>> listByProject(
            @RequestParam Long projectId,
            @RequestParam(value = "versionName", required = false) String versionName,
            @RequestParam(value = "moduleName", required = false) String moduleName
    ) {
        return ResultUtils.success(
                testCaseGenerationService.listByProject(projectId, versionName, moduleName)
        );
    }
}