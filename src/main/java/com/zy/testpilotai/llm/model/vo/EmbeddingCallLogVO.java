package com.zy.testpilotai.llm.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class EmbeddingCallLogVO {

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
     * 向量模型提供商/渠道
     */
    private String provider;

    /**
     * 具体调用的向量模型名称
     */
    private String modelName;

    /**
     * 接口调用状态
     */
    private String status;

    /**
     * 输入的原文本
     */
    private String inputText;

    /**
     * 向量维度
     */
    private Integer embeddingDimension;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * API 调用总耗时（毫秒）
     */
    private Long durationMs;

    /**
     * 调用发起/日志创建时间
     */
    private LocalDateTime createTime;
}