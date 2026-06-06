package com.zy.testpilotai.rageval.model.dto;

import lombok.Data;

@Data
public class RagEvalDatasetCreateRequest {

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
     * 评测集名称。
     */
    private String datasetName;

    /**
     * 描述。
     */
    private String description;
}