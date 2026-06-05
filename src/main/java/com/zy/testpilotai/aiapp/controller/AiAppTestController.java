package com.zy.testpilotai.aiapp.controller;

import com.zy.testpilotai.aiapp.model.dto.AiAppTestCaseListRequest;
import com.zy.testpilotai.aiapp.model.dto.AiAppTestGenerateRequest;
import com.zy.testpilotai.aiapp.model.vo.AiAppTestCaseVO;
import com.zy.testpilotai.aiapp.model.vo.AiAppTestGenerateResultVO;
import com.zy.testpilotai.aiapp.model.vo.AiAppTestTaskVO;
import com.zy.testpilotai.aiapp.service.AiAppTestService;
import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ai-app-test")
@RequiredArgsConstructor
public class AiAppTestController {

    private final AiAppTestService aiAppTestService;

    /**
     * 生成 AI 应用专项测试用例。
     */
    @PostMapping("/generate")
    public BaseResponse<AiAppTestGenerateResultVO> generate(
            @RequestBody @Valid AiAppTestGenerateRequest request
    ) {
        return ResultUtils.success(aiAppTestService.generate(request));
    }

    /**
     * 查询 AI 应用测试用例列表。
     */
    @PostMapping("/list")
    public BaseResponse<List<AiAppTestCaseVO>> list(
            @RequestBody AiAppTestCaseListRequest request
    ) {
        return ResultUtils.success(aiAppTestService.listCases(request));
    }

    /**
     * 查询 AI 应用测试任务详情。
     */
    @GetMapping("/task/{taskId}")
    public BaseResponse<AiAppTestTaskVO> getTask(
            @PathVariable String taskId
    ) {
        return ResultUtils.success(aiAppTestService.getTask(taskId));
    }
}