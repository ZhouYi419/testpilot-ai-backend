package com.zy.testpilotai.requirement.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.requirement.model.dto.ChangeImpactAnalyzeRequest;
import com.zy.testpilotai.requirement.model.dto.IncrementalTestCaseGenerateRequest;
import com.zy.testpilotai.requirement.model.vo.ChangeImpactAnalyzeResultVO;
import com.zy.testpilotai.requirement.model.vo.IncrementalTestCaseGenerateResultVO;
import com.zy.testpilotai.requirement.service.RequirementChangeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requirement")
@RequiredArgsConstructor
public class RequirementChangeController {

    private final RequirementChangeService requirementChangeService;

    /**
     * 新需求影响分析
     */
    @PostMapping("/change-impact-analyze")
    public BaseResponse<ChangeImpactAnalyzeResultVO> analyzeImpact(
            @RequestBody @Valid ChangeImpactAnalyzeRequest request
    ) {
        return ResultUtils.success(requirementChangeService.analyzeImpact(request));
    }

    /**
     * 查询影响分析任务详情
     */
    @GetMapping("/impact-task/{analysisTaskId}")
    public BaseResponse<ChangeImpactAnalyzeResultVO> getImpactTask(
            @PathVariable String analysisTaskId
    ) {
        return ResultUtils.success(requirementChangeService.getImpactTask(analysisTaskId));
    }

    /**
     * 基于影响分析结果生成新版本增量测试用例
     */
    @PostMapping("/generate-incremental-cases")
    public BaseResponse<IncrementalTestCaseGenerateResultVO> generateIncrementalCases(
            @RequestBody @Valid IncrementalTestCaseGenerateRequest request
    ) {
        return ResultUtils.success(requirementChangeService.generateIncrementalCases(request));
    }
}