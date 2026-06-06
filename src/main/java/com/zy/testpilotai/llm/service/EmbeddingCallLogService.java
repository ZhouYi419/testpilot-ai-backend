package com.zy.testpilotai.llm.service;

import com.zy.testpilotai.llm.model.dto.EmbeddingCallLogQueryRequest;
import com.zy.testpilotai.llm.model.vo.EmbeddingCallLogVO;
import java.util.List;

public interface EmbeddingCallLogService {

    void success(
            String bizType,
            String bizId,
            String provider,
            String modelName,
            String inputText,
            Integer embeddingDimension,
            Long durationMs
    );

    void failed(
            String bizType,
            String bizId,
            String provider,
            String modelName,
            String inputText,
            String errorMessage,
            Long durationMs
    );

    /**
     * 查询 Embedding 调用日志。
     */
    List<EmbeddingCallLogVO> list(EmbeddingCallLogQueryRequest request);
}