package com.zy.testpilotai.rageval.model.dto;

import lombok.Data;

@Data
public class RagEvalQuestionQueryRequest {

    /**
     * 评测集业务 ID。
     */
    private String datasetId;

    /**
     * 状态。
     */
    private String status;

    /**
     * 难度。
     */
    private String difficulty;

    /**
     * 关键词。
     */
    private String keyword;
}