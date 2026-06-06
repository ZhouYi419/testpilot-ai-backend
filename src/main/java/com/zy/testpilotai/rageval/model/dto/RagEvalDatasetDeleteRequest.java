package com.zy.testpilotai.rageval.model.dto;

import lombok.Data;

@Data
public class RagEvalDatasetDeleteRequest {

    /**
     * 评测集业务 ID。
     */
    private String datasetId;
}