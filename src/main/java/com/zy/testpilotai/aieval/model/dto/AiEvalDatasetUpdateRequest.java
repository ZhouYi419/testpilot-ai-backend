package com.zy.testpilotai.aieval.model.dto;

import lombok.Data;

@Data
public class AiEvalDatasetUpdateRequest {

    /**
     * 数据集业务 ID。
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
     * 数据集名称。
     */
    private String datasetName;

    /**
     * 数据集类型。
     */
    private String datasetType;

    /**
     * 描述。
     */
    private String description;
}