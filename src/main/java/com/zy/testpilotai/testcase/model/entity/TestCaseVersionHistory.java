package com.zy.testpilotai.testcase.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("test_case_version_history")
public class TestCaseVersionHistory {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 历史记录业务 ID。
     */
    private String historyId;

    /**
     * 关联对比任务 ID，可为空。
     */
    private String compareTaskId;

    /**
     * 快照类型：
     * MANUAL_SNAPSHOT / SOURCE_SNAPSHOT / TARGET_SNAPSHOT。
     */
    private String snapshotType;

    /**
     * 用例集业务 ID。
     */
    private String caseSetId;

    /**
     * 测试用例数据库 ID。
     */
    private Long testCaseId;

    /**
     * 项目 ID。
     */
    private Long projectId;

    /**
     * 版本号。
     */
    private String versionNo;

    /**
     * 模块编码。
     */
    private String moduleCode;

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
     * 来源类型。
     */
    private String sourceType;

    /**
     * 审核状态。
     */
    private String reviewStatus;

    /**
     * 内容哈希。
     */
    private String contentHash;

    private LocalDateTime createTime;
}