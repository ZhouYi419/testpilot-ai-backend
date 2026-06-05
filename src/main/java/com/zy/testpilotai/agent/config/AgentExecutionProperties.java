package com.zy.testpilotai.agent.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "agent.execution")
public class AgentExecutionProperties {

    /**
     * Agent 总任务超时时间，单位秒。
     */
    private Long taskTimeoutSeconds = 1800L;

    /**
     * Agent 单步骤超时时间，单位秒。
     */
    private Long stepTimeoutSeconds = 600L;
}