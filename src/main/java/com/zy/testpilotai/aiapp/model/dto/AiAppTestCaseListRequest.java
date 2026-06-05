package com.zy.testpilotai.aiapp.model.dto;

import lombok.Data;

@Data
public class AiAppTestCaseListRequest {

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * AI 应用类型
     */
    private String appType;

    /**
     * 测试维度
     */
    private String testDimension;

    /**
     * 风险等级
     */
    private String riskLevel;
}