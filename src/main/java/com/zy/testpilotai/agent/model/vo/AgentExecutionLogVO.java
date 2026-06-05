package com.zy.testpilotai.agent.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AgentExecutionLogVO {

    private Long id;

    /**
     * 关联的 Agent 任务ID
     */
    private String agentTaskId;

    /**
     * 步骤序号
     */
    private Integer stepIndex;

    /**
     * 步骤类型
     */
    private String stepType;

    /**
     * 步骤名称
     */
    private String stepName;

    /**
     * 日志级别
     */
    private String logLevel;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 简明摘要/提示信息
     */
    private String message;

    /**
     * 输入快照
     */
    private String inputSnapshot;

    /**
     * 输出快照
     */
    private String outputSnapshot;

    /**
     * 错误摘要信息
     */
    private String errorMessage;

    /**
     * 完整的错误堆栈信息
     */
    private String errorStack;

    /**
     * 大模型提供商
     */
    private String modelProvider;

    /**
     * 大模型具体版本/名称
     */
    private String modelName;

    /**
     * 提示词消耗的 Token 数量 (Prompt Tokens)
     */
    private Integer promptTokens;

    /**
     * 模型补全的 Token 数量 (Completion/Generation Tokens)
     */
    private Integer completionTokens;

    /**
     * 总计消耗的 Token 数量
     */
    private Integer totalTokens;

    /**
     * 当前步骤的执行耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 日志记录/生成时间
     */
    private LocalDateTime createTime;
}