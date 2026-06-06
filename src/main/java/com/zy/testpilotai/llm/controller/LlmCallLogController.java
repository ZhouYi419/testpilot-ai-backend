package com.zy.testpilotai.llm.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.llm.model.dto.LlmCallLogQueryRequest;
import com.zy.testpilotai.llm.model.vo.LlmCallLogVO;
import com.zy.testpilotai.llm.service.LlmCallLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/llm-call-log")
@RequiredArgsConstructor
public class LlmCallLogController {

    private final LlmCallLogService llmCallLogService;

    /**
     * 查询 LLM 调用日志。
     */
    @PostMapping("/list")
    public BaseResponse<List<LlmCallLogVO>> list(
            @RequestBody LlmCallLogQueryRequest request
    ) {
        return ResultUtils.success(llmCallLogService.list(request));
    }
}