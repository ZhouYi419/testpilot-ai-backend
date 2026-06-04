package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;

@Data
public class TestCaseQualityReviewResultVO {

    /**
     * 评审任务 ID
     */
    private String reviewTaskId;

    /**
     * 被评审的生成任务 ID
     */
    private String sourceTaskId;

    /**
     * 评审状态
     */
    private String status;

    /**
     * 总评分
     */
    private Double totalScore;

    /**
     * 完整评审结果 JSON
     */
    private String reviewResult;

    /**
     * 缺失测试点 JSON
     */
    private String missingPoints;

    /**
     * 建议补全方向 JSON
     */
    private String suggestedCaseDirections;

    /**
     * 模型原始输出
     */
    private String rawModelOutput;

    /**
     * 错误信息
     */
    private String errorMessage;
}