package com.zy.testpilotai.knowledge.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeEvaluateQueryRequest;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeEvaluateRequest;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeEvaluateResultVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeEvaluateTaskVO;
import com.zy.testpilotai.knowledge.service.KnowledgeEvaluateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/knowledge/evaluate")
@RequiredArgsConstructor
public class KnowledgeEvaluateController {

    private final KnowledgeEvaluateService knowledgeEvaluateService;

    /**
     * 执行知识库质量评估。
     */
    @PostMapping("/run")
    public BaseResponse<KnowledgeEvaluateResultVO> evaluate(
            @RequestBody KnowledgeEvaluateRequest request
    ) {
        return ResultUtils.success(knowledgeEvaluateService.evaluate(request));
    }

    /**
     * 查询评估任务列表。
     */
    @PostMapping("/list")
    public BaseResponse<List<KnowledgeEvaluateTaskVO>> list(
            @RequestBody KnowledgeEvaluateQueryRequest request
    ) {
        return ResultUtils.success(knowledgeEvaluateService.list(request));
    }

    /**
     * 查询评估详情。
     */
    @GetMapping("/{evaluateTaskId}")
    public BaseResponse<KnowledgeEvaluateResultVO> detail(
            @PathVariable String evaluateTaskId
    ) {
        return ResultUtils.success(knowledgeEvaluateService.detail(evaluateTaskId));
    }
}