package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestCaseVersionHistoryVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 历史记录全局唯一编号
     */
    private String historyId;

    /**
     * 关联的对比任务ID
     */
    private String compareTaskId;

    /**
     * 快照类型
     */
    private String snapshotType;

    /**
     * 归属的测试用例集ID
     */
    private String caseSetId;

    /**
     * 关联的原始测试用例主键ID
     */
    private Long testCaseId;

    /**
     * 项目ID
     */
    private Long projectId;

    /**
     * 快照生成时的文档版本号
     */
    private String versionNo;

    /**
     * 模块编码
     */
    private String moduleCode;

    /**
     * 用例标题
     */
    private String caseTitle;

    /**
     * 用例类型（如：功能、异常）
     */
    private String caseType;

    /**
     * 优先级（如：P0, P1）
     */
    private String priority;

    /**
     * 前置条件
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
     * 需求溯源引用（记录快照生成时的依赖来源）
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
     * 用例来源类型 (如：AI生成、手工创建)
     */
    private String sourceType;

    /**
     * 人工审核状态
     */
    private String reviewStatus;

    /**
     * 内容哈希值 (Hash)
     */
    private String contentHash;

    /**
     * 快照创建时间
     */
    private LocalDateTime createTime;
}