package com.zy.testpilotai.aieval.model.dto;

import lombok.Data;

@Data
public class AiEvalDatasetCreateRequest {

    /**
     * 项目 ID，可为空。
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
     * 数据集名称。
     */
    private String datasetName;

    /**
     * 数据集类型：
     * RAG / LLM / AGENT / PROMPT / SAFETY / MIXED。
     */
    private String datasetType;

    /**
     * 描述。
     */
    private String description;
}