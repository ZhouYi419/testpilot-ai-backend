package com.zy.testpilotai.rageval.model.dto;

import lombok.Data;

@Data
public class RagEvalDatasetQueryRequest {

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

    /**
     * 关键词。
     */
    private String keyword;
}