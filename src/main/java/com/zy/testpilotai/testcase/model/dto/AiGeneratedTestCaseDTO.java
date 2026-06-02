package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

import java.util.List;

/**
 * AI 生成的测试用例结构
 */
@Data
public class AiGeneratedTestCaseDTO {

    private String caseTitle;

    private String moduleName;

    private String priority;

    private String caseType;

    private String precondition;

    private List<String> steps;

    private String expectedResult;

    private Object testData;
}