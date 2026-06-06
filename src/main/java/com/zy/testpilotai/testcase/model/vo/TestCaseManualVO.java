package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestCaseManualVO {

    /**
     * 用例主键ID
     */
    private Long id;

    /**
     * 关联的 AI 生成任务ID
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
     * 测试用例标题
     */
    private String caseTitle;

    /**
     * 用例类型
     */
    private String caseType;

    /**
     * 用例优先级
     */
    private String priority;

    /**
     * 前置条件
     */
    private String precondition;

    /**
     * 测试执行步骤
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
     * 需求溯源引用
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
     * AI 给出的用例质量打分 (0-100)
     */
    private Double qualityScore;

    /**
     * 智能去重状态
     */
    private String duplicateStatus;

    /**
     * 重复关联的源用例ID
     */
    private Long duplicateOfCaseId;

    /**
     * 向量相似度/重复度得分
     */
    private Double duplicateScore;

    /**
     * 判定为重复的具体原因
     */
    private String duplicateReason;

    /**
     * 用例来源类型
     */
    private String sourceType;

    /**
     * 人工审核状态 (Human-in-the-loop 核心)
     */
    private String reviewStatus;

    /**
     * 审核人账号/姓名
     */
    private String reviewer;

    /**
     * 审核操作发生的时间
     */
    private LocalDateTime reviewTime;

    /**
     * 人工审核时的批注/意见
     */
    private String manualComment;

    /**
     * 最终被采纳/入库的时间
     */
    private LocalDateTime acceptedTime;

    /**
     * 被彻底驳回/废弃的时间
     */
    private LocalDateTime rejectedTime;

    /**
     * 记录创建时间
     */
    private LocalDateTime createTime;

    /**
     * 记录更新时间
     */
    private LocalDateTime updateTime;
}