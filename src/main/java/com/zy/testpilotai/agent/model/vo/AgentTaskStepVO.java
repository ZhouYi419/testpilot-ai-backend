package com.zy.testpilotai.agent.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AgentTaskStepVO {

    /**
     * 步骤 ID
     */
    private Long id;

    /**
     * Agent 任务 ID
     */
    private String agentTaskId;

    /**
     * 步骤序号
     */
    private Integer stepIndex;

    /**
     * 步骤名称
     */
    private String stepName;

    /**
     * 步骤状态
     */
    private String status;

    /**
     * 步骤输入 JSON
     */
    private String input;

    /**
     * 步骤输出 JSON
     */
    private String output;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 是否可重试
     */
    private Integer retryable;

    /**
     * 步骤类型
     */
    private String stepType;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;
}