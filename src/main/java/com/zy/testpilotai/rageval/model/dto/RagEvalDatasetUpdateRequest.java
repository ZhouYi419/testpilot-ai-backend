package com.zy.testpilotai.rageval.model.dto;

import lombok.Data;

@Data
public class RagEvalDatasetUpdateRequest {

    /**
     * 评测集业务 ID。
     */
    private String datasetId;

    /**
     * 版本号。
     */
    private String versionNo;

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 评测集名称。
     */
    private String datasetName;

    /**
     * 描述。
     */
    private String description;
}