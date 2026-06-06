package com.zy.testpilotai.llm.service;

import com.zy.testpilotai.llm.model.dto.LlmCallLogQueryRequest;
import com.zy.testpilotai.llm.model.vo.LlmCallLogVO;
import java.util.List;

public interface LlmCallLogService {

    /**
     * 记录成功调用
     */
    void success(
            String bizType,
            String bizId,
            String provider,
            String modelName,
            String systemPrompt,
            String userPrompt,
            String responseText,
            Long durationMs
    );

    /**
     * 记录失败调用
     */
    void failed(
            String bizType,
            String bizId,
            String provider,
            String modelName,
            String systemPrompt,
            String userPrompt,
            String errorMessage,
            Long durationMs
    );

    /**
     * 查询 LLM 调用日志。
     */
    List<LlmCallLogVO> list(LlmCallLogQueryRequest request);
}