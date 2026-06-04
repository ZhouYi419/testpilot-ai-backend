package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestCaseVO {

    /**
     * 用例 ID
     */
    private Long id;

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 版本号
     */
    private String versionNo;

    /**
     * 模块编码
     */
    private String moduleCode;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 用例标题
     */
    private String caseTitle;

    /**
     * 用例类型
     */
    private String caseType;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 前置条件
     */
    private String precondition;

    /**
     * 测试步骤 JSON 字符串
     */
    private String steps;

    /**
     * 预期结果
     */
    private String expectedResult;

    /**
     * 测试数据 JSON 字符串
     */
    private String testData;

    /**
     * 来源引用 JSON 字符串
     */
    private String sourceReferences;

    /**
     * 风险点
     */
    private String riskPoint;

    /**
     * 自动化建议
     */
    private String automationSuggestion;

    /**
     * 质量评分
     */
    private Double qualityScore;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}