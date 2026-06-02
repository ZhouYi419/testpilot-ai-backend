package com.zy.testpilotai.ai.embedding;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zy.testpilotai.ai.config.EmbeddingProperties;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import java.util.List;

/**
 * OpenAI Compatible Embedding 客户端
 */
@Component
@RequiredArgsConstructor
public class OpenAiCompatibleEmbeddingClient implements EmbeddingClient {

    private final EmbeddingProperties embeddingProperties;

    @Override
    public List<Double> embed(String text) {
        if (!StringUtils.hasText(text)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Embedding 文本不能为空");
        }

        if (!StringUtils.hasText(embeddingProperties.getBaseUrl())) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "Embedding base-url 未配置");
        }

        if (!StringUtils.hasText(embeddingProperties.getApiKey())) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "Embedding API Key 未配置，请配置环境变量 EMBEDDING_API_KEY");
        }

        if (!StringUtils.hasText(embeddingProperties.getModel())) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "Embedding model 未配置");
        }

        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(embeddingProperties.getBaseUrl())
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + embeddingProperties.getApiKey())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            EmbeddingRequest request = new EmbeddingRequest();
            request.setModel(embeddingProperties.getModel());
            request.setInput(text);

            EmbeddingResponse response = restClient.post()
                    .uri("/embeddings")
                    .body(request)
                    .retrieve()
                    .body(EmbeddingResponse.class);

            if (response == null || response.getData() == null || response.getData().isEmpty()) {
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "Embedding 接口返回为空");
            }

            List<Double> embedding = response.getData().get(0).getEmbedding();
            if (embedding == null || embedding.isEmpty()) {
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "Embedding 向量为空");
            }

            Integer expectedDimension = embeddingProperties.getDimension();
            if (expectedDimension != null && expectedDimension > 0 && embedding.size() != expectedDimension) {
                throw new BusinessException(
                        ErrorCode.AI_SERVICE_ERROR,
                        "Embedding 维度不匹配，期望：" + expectedDimension + "，实际：" + embedding.size()
                );
            }

            return embedding;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "调用 Embedding 接口失败：" + e.getMessage());
        }
    }

    @Override
    public String getModelName() {
        return embeddingProperties.getModel();
    }

    @Data
    private static class EmbeddingRequest {
        private String model;
        private String input;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EmbeddingResponse {
        private List<EmbeddingData> data;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class EmbeddingData {
        private Integer index;
        private List<Double> embedding;
    }
}