package com.zy.testpilotai.llm.embedding;

import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.llm.service.EmbeddingCallLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.embedding.provider", havingValue = "spring-ai")
public class SpringAiEmbeddingClient implements EmbeddingClient {

    private final EmbeddingModel embeddingModel;

    private final EmbeddingCallLogService embeddingCallLogService;

    @Value("${ai.embedding.spring-ai.model-name:text-embedding-v4}")
    private String modelName;

    @Value("${ai.embedding.dimension:1024}")
    private int dimension;

    @Override
    public String modelName() {
        return modelName;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public List<Float> embed(String text) {
        return embed(text, null, null);
    }

    @Override
    public List<Float> embed(String text, String bizType, String bizId) {
        if (!StringUtils.hasText(text)) {
            throw new BusinessException(
                    ErrorCode.AI_EMBEDDING_ERROR,
                    "Embedding 文本不能为空"
            );
        }

        long startMillis = System.currentTimeMillis();

        try {
            float[] vector = embeddingModel.embed(text);

            if (vector.length == 0) {
                throw new BusinessException(
                        ErrorCode.AI_EMBEDDING_ERROR,
                        "Spring AI EmbeddingModel 返回向量为空"
                );
            }

            if (vector.length != dimension) {
                throw new BusinessException(
                        ErrorCode.AI_EMBEDDING_ERROR,
                        "Embedding 向量维度不匹配，期望："
                                + dimension
                                + "，实际："
                                + vector.length
                );
            }

            List<Float> result = toFloatList(vector);

            long durationMs = System.currentTimeMillis() - startMillis;

            embeddingCallLogService.success(
                    bizType,
                    bizId,
                    "SPRING_AI",
                    modelName,
                    text,
                    vector.length,
                    durationMs
            );

            return result;
        } catch (BusinessException e) {
            long durationMs = System.currentTimeMillis() - startMillis;

            embeddingCallLogService.failed(
                    bizType,
                    bizId,
                    "SPRING_AI",
                    modelName,
                    text,
                    e.getMessage(),
                    durationMs
            );

            throw e;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startMillis;

            embeddingCallLogService.failed(
                    bizType,
                    bizId,
                    "SPRING_AI",
                    modelName,
                    text,
                    e.getMessage(),
                    durationMs
            );

            throw new BusinessException(
                    ErrorCode.AI_EMBEDDING_ERROR,
                    "Spring AI EmbeddingModel 调用失败，耗时 "
                            + durationMs
                            + " ms，原因："
                            + e.getMessage()
            );
        }
    }

    private List<Float> toFloatList(float[] vector) {
        List<Float> result = new ArrayList<>(vector.length);

        for (float value : vector) {
            result.add(value);
        }

        return result;
    }
}