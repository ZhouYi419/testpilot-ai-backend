package com.zy.testpilotai.llm.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("embedding_call_log")
public class EmbeddingCallLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 调用来源业务类型
     */
    private String bizType;

    /**
     * 业务 ID
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
     * 输入文本
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
     * 耗时，毫秒
     */
    private Long durationMs;

    private LocalDateTime createTime;
}