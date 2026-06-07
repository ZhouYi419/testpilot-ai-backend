package com.zy.testpilotai.aieval.model.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AiEvalCaseUpdateRequest {

    /**
     * 样本业务 ID。
     */
    private String caseId;

    /**
     * 测试类型。
     */
    private String caseType;

    /**
     * 测试维度。
     */
    private String testDimension;

    /**
     * 样本名称。
     */
    private String caseName;

    /**
     * 用户输入。
     */
    private String inputText;

    /**
     * 上下文。
     */
    private String contextText;

    /**
     * 期望行为。
     */
    private String expectedBehavior;

    /**
     * 标准答案。
     */
    private String expectedAnswer;

    /**
     * 期望关键词。
     */
    private List<String> expectedKeywords;

    /**
     * 禁止出现的关键词。
     */
    private List<String> forbiddenKeywords;

    /**
     * 期望工具名称。
     */
    private String expectedToolName;

    /**
     * 期望来源。
     */
    private List<Map<String, Object>> expectedSources;

    /**
     * 期望输出格式。
     */
    private String expectedOutputFormat;

    /**
     * 风险等级。
     */
    private String riskLevel;

    /**
     * 标签。
     */
    private List<String> tags;
}