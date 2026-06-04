package com.zy.testpilotai.llm.chat;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.chat.provider", havingValue = "dashscope")
public class DashScopeChatClient implements LlmClient {

    private final ObjectMapper objectMapper;

    /**
     * DashScope API Key。
     */
    @Value("${ai.chat.dashscope.api-key:}")
    private String apiKey;

    /**
     * DashScope OpenAI Compatible Chat Completions endpoint。
     */
    @Value("${ai.chat.dashscope.endpoint}")
    private String endpoint;

    /**
     * 聊天模型名称，例如 qwen-plus。
     */
    @Value("${ai.chat.dashscope.model-name:qwen-plus}")
    private String modelName;

    /**
     * 模型温度。
     */
    @Value("${ai.chat.dashscope.temperature:0.2}")
    private Double temperature;

    /**
     * HTTP 超时时间。
     */
    @Value("${ai.chat.dashscope.timeout-seconds:120}")
    private int timeoutSeconds;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public String modelName() {
        return modelName;
    }

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(
                    ErrorCode.AI_CHAT_ERROR,
                    "DashScope API Key 为空，请配置环境变量 DASHSCOPE_API_KEY"
            );
        }

        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", modelName);
            body.put("temperature", temperature);
            body.put("messages", List.of(
                    Map.of(
                            "role", "system",
                            "content", systemPrompt
                    ),
                    Map.of(
                            "role", "user",
                            "content", userPrompt
                    )
            ));

            String jsonBody = objectMapper.writeValueAsString(body);

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
                        ErrorCode.AI_CHAT_ERROR,
                        "DashScope Chat 请求失败，HTTP 状态码："
                                + response.statusCode()
                                + "，响应："
                                + response.body()
                );
            }

            return parseContent(response.body());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_CHAT_ERROR,
                    "DashScope Chat 调用异常：" + e.getMessage()
            );
        }
    }

    private String parseContent(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode choicesNode = root.path("choices");

            if (!choicesNode.isArray() || choicesNode.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.AI_CHAT_ERROR,
                        "DashScope Chat 响应 choices 为空：" + responseBody
                );
            }

            String content = choicesNode.get(0)
                    .path("message")
                    .path("content")
                    .asText();

            if (!StringUtils.hasText(content)) {
                throw new BusinessException(
                        ErrorCode.AI_CHAT_ERROR,
                        "DashScope Chat 响应 content 为空：" + responseBody
                );
            }

            return content;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_CHAT_ERROR,
                    "解析 DashScope Chat 响应失败：" + e.getMessage()
            );
        }
    }
}