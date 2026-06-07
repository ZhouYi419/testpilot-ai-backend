package com.zy.testpilotai.testcase.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.testcase.model.dto.TestCaseEmbeddingBuildRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSemanticDeduplicateRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseEmbeddingBuildResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseSemanticDeduplicateResultVO;
import com.zy.testpilotai.testcase.service.TestCaseSemanticDeduplicateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/testcase/semantic-deduplicate")
@RequiredArgsConstructor
public class TestCaseSemanticDeduplicateController {

    private final TestCaseSemanticDeduplicateService testCaseSemanticDeduplicateService;

    /**
     * 构建测试用例 Embedding。
     */
    @PostMapping("/build-embedding")
    public BaseResponse<TestCaseEmbeddingBuildResultVO> buildEmbedding(
            @RequestBody TestCaseEmbeddingBuildRequest request
    ) {
        return ResultUtils.success(testCaseSemanticDeduplicateService.buildEmbeddings(request));
    }

    /**
     * 执行语义去重。
     */
    @PostMapping("/run")
    public BaseResponse<TestCaseSemanticDeduplicateResultVO> deduplicate(
            @RequestBody TestCaseSemanticDeduplicateRequest request
    ) {
        return ResultUtils.success(testCaseSemanticDeduplicateService.deduplicate(request));
    }

    /**
     * 查询语义去重任务详情。
     */
    @GetMapping("/{deduplicateTaskId}")
    public BaseResponse<TestCaseSemanticDeduplicateResultVO> detail(
            @PathVariable String deduplicateTaskId
    ) {
        return ResultUtils.success(testCaseSemanticDeduplicateService.detail(deduplicateTaskId));
    }
}