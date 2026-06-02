package com.zy.testpilotai.ai.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.zy.testpilotai.ai.config.ChatProperties;
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

@Component
@RequiredArgsConstructor
public class OpenAiCompatibleLlmClient implements LlmClient {

    private final ChatProperties chatProperties;

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        if (!StringUtils.hasText(systemPrompt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "systemPrompt 不能为空");
        }

        if (!StringUtils.hasText(userPrompt)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "userPrompt 不能为空");
        }

        if (!StringUtils.hasText(chatProperties.getBaseUrl())) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "LLM base-url 未配置");
        }

        if (!StringUtils.hasText(chatProperties.getApiKey())) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "LLM API Key 未配置，请配置环境变量 LLM_API_KEY");
        }

        if (!StringUtils.hasText(chatProperties.getModel())) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "LLM model 未配置");
        }

        try {
            RestClient restClient = RestClient.builder()
                    .baseUrl(chatProperties.getBaseUrl())
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + chatProperties.getApiKey())
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();

            ChatCompletionRequest request = new ChatCompletionRequest();
            request.setModel(chatProperties.getModel());
            request.setTemperature(chatProperties.getTemperature());
            request.setMaxTokens(chatProperties.getMaxTokens());
            request.setMessages(List.of(
                    new ChatMessage("system", systemPrompt),
                    new ChatMessage("user", userPrompt)
            ));

            ChatCompletionResponse response = restClient.post()
                    .uri("/chat/completions")
                    .body(request)
                    .retrieve()
                    .body(ChatCompletionResponse.class);

            if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "LLM 返回为空");
            }

            ChatMessage message = response.getChoices().get(0).getMessage();
            if (message == null || !StringUtils.hasText(message.getContent())) {
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "LLM 回答内容为空");
            }

            return message.getContent().trim();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "调用 LLM 失败：" + e.getMessage());
        }
    }

    @Override
    public String getModelName() {
        return chatProperties.getModel();
    }

    @Data
    private static class ChatCompletionRequest {

        private String model;

        private List<ChatMessage> messages;

        private Double temperature;

        @JsonProperty("max_tokens")
        private Integer maxTokens;
    }

    @Data
    private static class ChatMessage {

        private String role;

        private String content;

        public ChatMessage() {
        }

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class ChatCompletionResponse {

        private List<Choice> choices;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Choice {

        private Integer index;

        private ChatMessage message;
    }
}