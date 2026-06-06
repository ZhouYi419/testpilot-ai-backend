package com.zy.testpilotai.llm.model.dto;

import lombok.Data;

@Data
public class LlmCallLogQueryRequest {

    /**
     * 业务类型
     */
    private String bizType;

    /**
     * 业务 ID
     */
    private String bizId;

    /**
     * 调用状态
     */
    private String status;

    /**
     * 模型供应商
     */
    private String provider;
}