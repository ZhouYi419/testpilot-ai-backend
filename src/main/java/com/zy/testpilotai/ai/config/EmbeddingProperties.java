package com.zy.testpilotai.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "testpilot.ai.embedding")
public class EmbeddingProperties {

    /**
     * OpenAI Compatible API 地址，例如：
     * https://api.openai.com/v1
     */
    private String baseUrl;

    /**
     * API Key
     */
    private String apiKey;

    /**
     * Embedding 模型名称
     */
    private String model;

    /**
     * 向量维度，必须和数据库 vector(n) 保持一致
     */
    private Integer dimension = 1536;
}