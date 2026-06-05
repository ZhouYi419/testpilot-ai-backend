package com.zy.testpilotai.agent.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AgentRetryRequest {

    /**
     * Agent 任务 ID
     */
    @NotBlank(message = "Agent任务ID不能为空")
    private String agentTaskId;

    /**
     * 从第几步开始重试。
     */
    @NotNull(message = "重试起始步骤不能为空")
    private Integer startStepIndex;
}