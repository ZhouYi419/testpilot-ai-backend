package com.zy.testpilotai.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "testpilot.ai.chat")
public class ChatProperties {

    /**
     * OpenAI Compatible API 地址
     */
    private String baseUrl;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 对话模型名称
     */
    private String model;

    /**
     * 温度，越低越稳定
     */
    private Double temperature = 0.2;

    /**
     * 最大输出 token
     */
    private Integer maxTokens = 2000;
}