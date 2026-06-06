package com.zy.testpilotai.llm.structured.dto;

import lombok.Data;

/**
 * AI 测试用例质量评审结构化输出。
 */
@Data
public class AiTestCaseReviewOutputDTO {

    /**
     * 总评分，0 - 100。
     */
    private Integer totalScore;

    /**
     * 评分维度 JSON。
     */
    private String dimensions;

    /**
     * 缺失测试点 JSON。
     */
    private String missingPoints;

    /**
     * 重复用例 JSON。
     */
    private String duplicateCases;

    /**
     * 低质量用例 JSON。
     */
    private String lowQualityCases;

    /**
     * 整体评审总结。
     */
    private String summary;
}