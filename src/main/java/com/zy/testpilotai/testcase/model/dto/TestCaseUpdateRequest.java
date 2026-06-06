package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseUpdateRequest {

    private Long id;

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
     * 测试步骤 JSON。
     */
    private String steps;

    /**
     * 预期结果。
     */
    private String expectedResult;

    /**
     * 测试数据 JSON。
     */
    private String testData;

    /**
     * 来源引用 JSON。
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

    /**
     * 审核人。
     */
    private String reviewer;

    /**
     * 人工备注。
     */
    private String manualComment;
}