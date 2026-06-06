package com.zy.testpilotai.llm.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.llm.model.dto.EmbeddingCallLogQueryRequest;
import com.zy.testpilotai.llm.model.vo.EmbeddingCallLogVO;
import com.zy.testpilotai.llm.service.EmbeddingCallLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/embedding-call-log")
@RequiredArgsConstructor
public class EmbeddingCallLogController {

    private final EmbeddingCallLogService embeddingCallLogService;

    /**
     * 查询 Embedding 调用日志。
     */
    @PostMapping("/list")
    public BaseResponse<List<EmbeddingCallLogVO>> list(
            @RequestBody EmbeddingCallLogQueryRequest request
    ) {
        return ResultUtils.success(embeddingCallLogService.list(request));
    }
}