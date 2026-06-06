package com.zy.testpilotai.llm.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LlmCallLogVO {

    private Long id;

    /**
     * 关联的业务类型
     */
    private String bizType;

    /**
     * 关联的业务主键ID
     */
    private String bizId;

    /**
     * 大模型提供商/渠道
     */
    private String provider;

    /**
     * 具体调用的模型名称
     */
    private String modelName;

    /**
     * 接口调用状态
     */
    private String status;

    /**
     * 系统提示词 (System Prompt)
     */
    private String systemPrompt;

    /**
     * 用户提示词/完整输入 (User Prompt / Messages)
     */
    private String userPrompt;

    /**
     * 大模型的原始响应文本
     */
    private String responseText;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 输入提示词消耗的 Token 数
     */
    private Integer promptTokens;

    /**
     * 模型生成的 Token 数
     */
    private Integer completionTokens;

    /**
     * 总计 Token 数
     */
    private Integer totalTokens;

    /**
     * 网络请求与模型生成的总耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 调用发起/日志创建时间
     */
    private LocalDateTime createTime;
}