package com.zy.testpilotai.aieval.model.dto;

import lombok.Data;

@Data
public class AiEvalAppConfigQueryRequest {

    /**
     * 应用类型。
     */
    private String appType;

    /**
     * 状态。
     */
    private String status;

    /**
     * 关键词，匹配配置名称 / 地址 / 描述。
     */
    private String keyword;
}