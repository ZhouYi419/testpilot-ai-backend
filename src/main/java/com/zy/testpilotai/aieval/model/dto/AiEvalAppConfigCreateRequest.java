package com.zy.testpilotai.aieval.model.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AiEvalAppConfigCreateRequest {

    /**
     * 配置名称。
     */
    private String configName;

    /**
     * 应用类型：
     * RAG / LLM / AGENT / PROMPT / MIXED。
     */
    private String appType;

    /**
     * 接口地址。
     */
    private String endpointUrl;

    /**
     * HTTP 方法。
     */
    private String httpMethod;

    /**
     * 鉴权类型。
     */
    private String authType;

    /**
     * 鉴权 Header 名称。
     */
    private String authHeaderName;

    /**
     * API Key。
     */
    private String apiKey;

    /**
     * 固定请求 Header。
     */
    private Map<String, String> headers;

    /**
     * 请求体模板。
     */
    private String requestBodyTemplate;

    /**
     * 响应 JSON 路径。
     */
    private String responseJsonPath;

    /**
     * 超时时间，单位秒。
     */
    private Integer timeoutSeconds;

    /**
     * 描述。
     */
    private String description;
}