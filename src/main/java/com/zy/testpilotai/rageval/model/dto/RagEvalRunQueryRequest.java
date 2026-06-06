package com.zy.testpilotai.rageval.model.dto;

import lombok.Data;

@Data
public class RagEvalRunQueryRequest {

    /**
     * 评测集业务 ID。
     */
    private String datasetId;

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
     * 状态。
     */
    private String status;
}