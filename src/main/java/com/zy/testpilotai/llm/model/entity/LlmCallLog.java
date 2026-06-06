package com.zy.testpilotai.llm.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("llm_call_log")
public class LlmCallLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 调用来源业务类型
     */
    private String bizType;

    /**
     * 业务任务 ID
     */
    private String bizId;

    /**
     * 模型供应商
     */
    private String provider;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 调用状态
     */
    private String status;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 用户提示词
     */
    private String userPrompt;

    /**
     * 模型响应文本
     */
    private String responseText;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 输入 token
     */
    private Integer promptTokens;

    /**
     * 输出 token
     */
    private Integer completionTokens;

    /**
     * 总 token
     */
    private Integer totalTokens;

    /**
     * 耗时，毫秒
     */
    private Long durationMs;

    private LocalDateTime createTime;
}