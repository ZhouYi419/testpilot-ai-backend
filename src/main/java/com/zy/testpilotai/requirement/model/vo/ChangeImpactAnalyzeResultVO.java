package com.zy.testpilotai.requirement.model.vo;

import lombok.Data;

@Data
public class ChangeImpactAnalyzeResultVO {

    /**
     * 影响分析任务 ID
     */
    private String analysisTaskId;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 基线版本
     */
    private String baseVersionNo;

    /**
     * 目标版本
     */
    private String targetVersionNo;

    /**
     * 新需求
     */
    private String newRequirement;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 变更摘要 JSON
     */
    private String changeSummary;

    /**
     * 影响模块 JSON
     */
    private String affectedModules;

    /**
     * 相关旧规则 JSON
     */
    private String relatedOldRules;

    /**
     * 相关历史用例 JSON
     */
    private String relatedHistoricalCases;

    /**
     * 风险点 JSON
     */
    private String riskPoints;

    /**
     * 回归范围 JSON
     */
    private String regressionScope;

    /**
     * 建议新增测试点 JSON
     */
    private String suggestedNewTestPoints;

    /**
     * 模型原始输出
     */
    private String rawModelOutput;

    /**
     * 错误信息
     */
    private String errorMessage;
}