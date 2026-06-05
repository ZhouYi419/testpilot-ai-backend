package com.zy.testpilotai.aiapp.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiAppTestCaseVO {

    /**
     * 用例 ID
     */
    private Long id;

    /**
     * 所属任务 ID
     */
    private String taskId;

    /**
     * AI 应用类型
     */
    private String appType;

    /**
     * 测试维度
     */
    private String testDimension;

    /**
     * 用例标题
     */
    private String caseTitle;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 攻击 Prompt 或测试输入 Prompt
     */
    private String attackPrompt;

    /**
     * 输入数据 JSON
     */
    private String inputData;

    /**
     * 前置条件
     */
    private String precondition;

    /**
     * 测试步骤 JSON
     */
    private String steps;

    /**
     * 预期行为
     */
    private String expectedBehavior;

    /**
     * 通过标准
     */
    private String passCriteria;

    /**
     * 评估方式
     */
    private String evaluationMethod;

    /**
     * 风险等级
     */
    private String riskLevel;

    /**
     * 自动化建议
     */
    private String automationSuggestion;

    /**
     * 来源引用 JSON
     */
    private String sourceReferences;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}