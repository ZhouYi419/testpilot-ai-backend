package com.zy.testpilotai.aieval.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiEvalAppConfigVO {

    private Long id;

    private String appConfigId;

    private String configName;

    private String appType;

    private String endpointUrl;

    private String httpMethod;

    private String authType;

    private String authHeaderName;

    private String headers;

    private String requestBodyTemplate;

    private String responseJsonPath;

    private Integer timeoutSeconds;

    private String description;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}