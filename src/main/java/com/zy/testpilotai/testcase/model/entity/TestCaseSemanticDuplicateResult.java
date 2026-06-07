package com.zy.testpilotai.testcase.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("test_case_semantic_duplicate_result")
public class TestCaseSemanticDuplicateResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 语义去重任务业务 ID。
     */
    private String deduplicateTaskId;

    /**
     * 源用例 ID。
     */
    private Long sourceTestCaseId;

    /**
     * 相似用例 ID。
     */
    private Long targetTestCaseId;

    /**
     * 源用例标题。
     */
    private String sourceCaseTitle;

    /**
     * 相似用例标题。
     */
    private String targetCaseTitle;

    /**
     * 相似度。
     */
    private Double similarity;

    /**
     * 对比范围。
     */
    private String compareScope;

    /**
     * 是否已经标记重复：
     * 1：是
     * 0：否。
     */
    private Integer markedDuplicate;

    /**
     * 重复原因。
     */
    private String duplicateReason;

    private LocalDateTime createTime;
}