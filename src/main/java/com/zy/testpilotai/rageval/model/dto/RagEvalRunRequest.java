package com.zy.testpilotai.rageval.model.dto;

import lombok.Data;

@Data
public class RagEvalRunRequest {

    /**
     * 评测集业务 ID。
     */
    private String datasetId;

    /**
     * 召回数量。
     */
    private Integer topK;

    /**
     * 是否覆盖评测集版本号。
     * 不传则使用评测集里的 versionNo。
     */
    private String versionNo;

    /**
     * 是否覆盖评测集模块编码。
     * 不传则使用评测集里的 moduleCode。
     */
    private String moduleCode;
}