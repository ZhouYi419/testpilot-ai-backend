package com.zy.testpilotai.automation.model.dto;

import lombok.Data;
import java.util.Map;

@Data
public class AutomationRunStartRequest {

    /**
     * 脚本生成任务业务 ID。
     */
    private String scriptTaskId;

    /**
     * 执行环境名称。
     */
    private String environmentName;

    /**
     * 覆盖脚本中的 API_BASE_URL。
     */
    private String baseUrl;

    /**
     * API Token，会写入环境变量 API_TOKEN。
     */
    private String apiToken;

    /**
     * 额外环境变量。
     */
    private Map<String, String> extraEnv;

    /**
     * 超时时间，单位秒。
     */
    private Integer timeoutSeconds;
}