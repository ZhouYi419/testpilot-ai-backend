package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;

@Data
public class DuplicateCaseVO {

    /**
     * 被标记为重复的用例 ID
     */
    private Long caseId;

    /**
     * 被标记为重复的用例标题
     */
    private String caseTitle;

    /**
     * 重复目标用例 ID
     */
    private Long duplicateOfCaseId;

    /**
     * 重复目标用例标题
     */
    private String duplicateOfCaseTitle;

    /**
     * 相似度分数
     */
    private Double similarityScore;

    /**
     * 重复原因
     */
    private String duplicateReason;
}