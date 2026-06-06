package com.zy.testpilotai.llm.embedding;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.embedding.provider", havingValue = "dashscope", matchIfMissing = true)
public class DashScopeEmbeddingClient implements EmbeddingClient {

    private final ObjectMapper objectMapper;

    @Value("${ai.embedding.dashscope.api-key:}")
    private String apiKey;

    @Value("${ai.embedding.dashscope.endpoint}")
    private String endpoint;

    @Value("${ai.embedding.dashscope.model-name:text-embedding-v4}")
    private String modelName;

    @Value("${ai.embedding.dimension:1024}")
    private int dimension;

    @Value("${ai.embedding.dashscope.timeout-seconds:60}")
    private int timeoutSeconds;

    private final HttpClient httpClient = HttpClient.newHttpClient();

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
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(
                    ErrorCode.AI_EMBEDDING_ERROR,
                    "DashScope API Key 为空，请配置环境变量 DASHSCOPE_API_KEY"
            );
        }

        if (text == null) {
            text = "";
        }

        long startMillis = System.currentTimeMillis();

        try {
            Map<String, Object> requestBody = new LinkedHashMap<>();
            requestBody.put("model", modelName);
            requestBody.put("input", text);
            requestBody.put("dimensions", dimension);
            requestBody.put("encoding_format", "float");

            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new BusinessException(
                        ErrorCode.AI_EMBEDDING_ERROR,
                        "DashScope Embedding 请求失败，HTTP 状态码："
                                + response.statusCode()
                                + "，响应："
                                + response.body()
                );
            }

            return parseEmbedding(response.body());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startMillis;

            throw new BusinessException(
                    ErrorCode.AI_EMBEDDING_ERROR,
                    "DashScope Embedding 调用异常，耗时 "
                            + durationMs
                            + " ms，原因："
                            + e.getMessage()
            );
        }
    }

    private List<Float> parseEmbedding(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);

            JsonNode dataNode = root.path("data");
            if (!dataNode.isArray() || dataNode.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.AI_EMBEDDING_ERROR,
                        "DashScope Embedding 响应中 data 为空：" + responseBody
                );
            }

            JsonNode embeddingNode = dataNode.get(0).path("embedding");
            if (!embeddingNode.isArray() || embeddingNode.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.AI_EMBEDDING_ERROR,
                        "DashScope Embedding 响应中 embedding 为空：" + responseBody
                );
            }

            List<Float> vector = new ArrayList<>(embeddingNode.size());

            for (JsonNode item : embeddingNode) {
                vector.add((float) item.asDouble());
            }

            if (vector.size() != dimension) {
                throw new BusinessException(
                        ErrorCode.AI_EMBEDDING_ERROR,
                        "DashScope Embedding 维度不匹配，期望："
                                + dimension
                                + "，实际："
                                + vector.size()
                );
            }

            return vector;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_EMBEDDING_ERROR,
                    "解析 DashScope Embedding 响应失败：" + e.getMessage()
            );
        }
    }
}