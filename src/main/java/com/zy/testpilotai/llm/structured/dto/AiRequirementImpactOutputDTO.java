package com.zy.testpilotai.llm.structured.dto;

import lombok.Data;

@Data
public class AiRequirementImpactOutputDTO {

    /**
     * 变更摘要 JSON。
     */
    private String changeSummary;

    /**
     * 影响模块 JSON。
     */
    private String affectedModules;

    /**
     * 相关旧规则 JSON。
     */
    private String relatedOldRules;

    /**
     * 风险点 JSON。
     */
    private String riskPoints;

    /**
     * 回归范围 JSON。
     */
    private String regressionScope;

    /**
     * 建议新增测试点 JSON。
     */
    private String suggestedNewTestPoints;

    /**
     * 总结。
     */
    private String summary;
}