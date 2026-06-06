package com.zy.testpilotai.llm.structured.dto;

import lombok.Data;

@Data
public class AiAppTestCaseItemDTO {

    /**
     * AI 应用类型：LLM / RAG / AGENT / PROMPT / AI_APP
     */
    private String appType;

    /**
     * 测试维度。
     */
    private String testDimension;

    /**
     * 用例标题。
     */
    private String caseTitle;

    /**
     * 优先级：P0 / P1 / P2 / P3
     */
    private String priority;

    /**
     * 攻击 Prompt 或测试输入 Prompt。
     */
    private String attackPrompt;

    /**
     * 输入数据 JSON 字符串。
     */
    private String inputData;

    /**
     * 前置条件。
     */
    private String precondition;

    /**
     * 测试步骤 JSON 字符串。
     */
    private String steps;

    /**
     * 预期行为。
     */
    private String expectedBehavior;

    /**
     * 通过标准。
     */
    private String passCriteria;

    /**
     * 评估方式。
     */
    private String evaluationMethod;

    /**
     * 风险等级：HIGH / MEDIUM / LOW
     */
    private String riskLevel;

    /**
     * 自动化建议。
     */
    private String automationSuggestion;

    /**
     * 来源引用 JSON 字符串。
     */
    private String sourceReferences;
}