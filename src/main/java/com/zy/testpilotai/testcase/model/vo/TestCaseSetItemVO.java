package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestCaseSetItemVO {

    /**
     * 关联项主键ID
     */
    private Long itemId;

    /**
     * 所属的用例集业务编号
     */
    private String caseSetId;

    /**
     * 关联的具体测试用例主键ID
     */
    private Long testCaseId;

    /**
     * 在该用例集中的展示/执行顺序
     */
    private Integer itemOrder;

    /**
     * 生成此用例的 AI 任务ID
     */
    private String taskId;

    /**
     * 归属的项目ID
     */
    private Long projectId;

    /**
     * 关联的文档版本号
     */
    private String versionNo;

    /**
     * 关联的模块编码
     */
    private String moduleCode;

    /**
     * 关联的模块名称
     */
    private String moduleName;

    /**
     * 用例标题
     */
    private String caseTitle;

    /**
     * 用例类型 (如：功能测试、异常测试)
     */
    private String caseType;

    /**
     * 用例优先级 (如：P0, P1, P2)
     */
    private String priority;

    /**
     * 执行前置条件
     */
    private String precondition;

    /**
     * 测试步骤
     */
    private String steps;

    /**
     * 预期结果
     */
    private String expectedResult;

    /**
     * 测试数据
     */
    private String testData;

    /**
     * 需求溯源引用（记录由哪段文档或 Chunk 生成）
     */
    private String sourceReferences;

    /**
     * 风险提示/测试注意点
     */
    private String riskPoint;

    /**
     * 自动化测试建议
     */
    private String automationSuggestion;

    /**
     * AI 用例质量打分
     */
    private Double qualityScore;

    /**
     * 智能去重状态
     */
    private String duplicateStatus;

    /**
     * 用例来源类型 (如：AI生成、手工创建)
     */
    private String sourceType;

    /**
     * 人工审核状态 (如：已采纳、待审核)
     */
    private String reviewStatus;

    /**
     * 审核人
     */
    private String reviewer;

    /**
     * 审核批注/修改意见
     */
    private String manualComment;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}