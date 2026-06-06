package com.zy.testpilotai.llm.structured.dto;

import lombok.Data;

@Data
public class AiGeneratedTestCaseItemDTO {

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 模块名称。
     */
    private String moduleName;

    /**
     * 用例标题。
     */
    private String caseTitle;

    /**
     * 用例类型。
     */
    private String caseType;

    /**
     * 优先级。
     */
    private String priority;

    /**
     * 前置条件。
     */
    private String precondition;

    /**
     * 测试步骤，JSON 字符串。
     *
     * 兼容：
     * 1. ["步骤1", "步骤2"]
     * 2. "1. 步骤1\n2. 步骤2"
     * 3. {"step1":"xxx"}
     */
    private String steps;

    /**
     * 预期结果。
     */
    private String expectedResult;

    /**
     * 测试数据，JSON 字符串。
     */
    private String testData;

    /**
     * 来源引用，JSON 字符串。
     */
    private String sourceReferences;

    /**
     * 风险点。
     */
    private String riskPoint;

    /**
     * 自动化建议。
     */
    private String automationSuggestion;
}