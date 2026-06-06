package com.zy.testpilotai.llm.chat;

import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.llm.service.LlmCallLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "ai.chat.provider", havingValue = "spring-ai")
public class SpringAiLlmClient implements LlmClient {

    private final ChatClient.Builder chatClientBuilder;

    private final LlmCallLogService llmCallLogService;

    @Value("${ai.chat.spring-ai.model-name:qwen-plus}")
    private String modelName;

    @Override
    public String modelName() {
        return modelName;
    }

    @Override
    public String chat(String systemPrompt, String userPrompt) {
        return chat(systemPrompt, userPrompt, null, null);
    }

    @Override
    public String chat(
            String systemPrompt,
            String userPrompt,
            String bizType,
            String bizId
    ) {
        if (!StringUtils.hasText(systemPrompt)) {
            systemPrompt = "你是一个专业 AI 助手。";
        }

        if (!StringUtils.hasText(userPrompt)) {
            throw new BusinessException(
                    ErrorCode.AI_CHAT_ERROR,
                    "用户提示词不能为空"
            );
        }

        long startMillis = System.currentTimeMillis();

        try {
            ChatClient chatClient = chatClientBuilder.build();

            String content = chatClient
                    .prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            long durationMs = System.currentTimeMillis() - startMillis;

            if (!StringUtils.hasText(content)) {
                throw new BusinessException(
                        ErrorCode.AI_CHAT_ERROR,
                        "Spring AI ChatClient 返回内容为空"
                );
            }

            llmCallLogService.success(
                    bizType,
                    bizId,
                    "SPRING_AI",
                    modelName,
                    systemPrompt,
                    userPrompt,
                    content,
                    durationMs
            );

            return content;
        } catch (BusinessException e) {
            long durationMs = System.currentTimeMillis() - startMillis;

            llmCallLogService.failed(
                    bizType,
                    bizId,
                    "SPRING_AI",
                    modelName,
                    systemPrompt,
                    userPrompt,
                    e.getMessage(),
                    durationMs
            );

            throw e;
        } catch (Exception e) {
            long durationMs = System.currentTimeMillis() - startMillis;

            llmCallLogService.failed(
                    bizType,
                    bizId,
                    "SPRING_AI",
                    modelName,
                    systemPrompt,
                    userPrompt,
                    e.getMessage(),
                    durationMs
            );

            throw new BusinessException(
                    ErrorCode.AI_CHAT_ERROR,
                    "Spring AI ChatClient 调用失败，耗时 "
                            + durationMs
                            + " ms，原因："
                            + e.getMessage()
            );
        }
    }
}