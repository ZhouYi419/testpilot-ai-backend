package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestCaseSetCompareResultVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 所属的对比任务ID
     */
    private String compareTaskId;

    /**
     * 差异/变更类型
     */
    private String resultType;

    /**
     * 源测试用例主键ID
     */
    private Long sourceTestCaseId;

    /**
     * 目标测试用例主键ID
     */
    private Long targetTestCaseId;

    /**
     * 源测试用例标题
     */
    private String sourceCaseTitle;

    /**
     * 目标测试用例标题
     */
    private String targetCaseTitle;

    /**
     * 单条用例的变更摘要
     */
    private String changeSummary;

    /**
     * 字段级别的差异明细
     */
    private String fieldDiffs;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}