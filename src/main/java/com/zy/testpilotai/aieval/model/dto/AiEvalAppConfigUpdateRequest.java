package com.zy.testpilotai.aieval.model.dto;

import lombok.Data;

import java.util.Map;

@Data
public class AiEvalAppConfigUpdateRequest {

    /**
     * 应用配置 ID。
     */
    private String appConfigId;

    private String configName;

    private String appType;

    private String endpointUrl;

    private String httpMethod;

    private String authType;

    private String authHeaderName;

    private String apiKey;

    private Map<String, String> headers;

    private String requestBodyTemplate;

    private String responseJsonPath;

    private Integer timeoutSeconds;

    private String description;
}