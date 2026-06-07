package com.zy.testpilotai.aieval.model.dto;

import lombok.Data;

@Data
public class AiEvalCaseQueryRequest {

    /**
     * 数据集业务 ID。
     */
    private String datasetId;

    /**
     * 测试类型。
     */
    private String caseType;

    /**
     * 测试维度。
     */
    private String testDimension;

    /**
     * 风险等级。
     */
    private String riskLevel;

    /**
     * 状态。
     */
    private String status;

    /**
     * 关键词，匹配样本名称 / 输入 / 期望行为。
     */
    private String keyword;
}