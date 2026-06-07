package com.zy.testpilotai.aieval.model.dto;

import lombok.Data;

@Data
public class AiEvalDatasetQueryRequest {

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
     * 数据集类型。
     */
    private String datasetType;

    /**
     * 状态。
     */
    private String status;

    /**
     * 关键词，匹配数据集名称 / 描述。
     */
    private String keyword;
}