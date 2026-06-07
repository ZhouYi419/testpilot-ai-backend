package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestCaseSemanticDuplicatePairVO {

    private Long id;

    /**
     * 关联的去重任务ID
     */
    private String deduplicateTaskId;

    /**
     * 源测试用例ID
     */
    private Long sourceTestCaseId;

    /**
     * 目标测试用例ID
     */
    private Long targetTestCaseId;

    /**
     * 源用例标题
     */
    private String sourceCaseTitle;

    /**
     * 目标用例标题
     */
    private String targetCaseTitle;

    /**
     * 语义相似度得分
     */
    private Double similarity;

    /**
     * 对比范围
     */
    private String compareScope;

    /**
     * 是否确认为重复项
     */
    private Integer markedDuplicate;

    /**
     * 判定重复/去重的具体原因
     */
    private String duplicateReason;

    /**
     * 记录生成时间
     */
    private LocalDateTime createTime;
}