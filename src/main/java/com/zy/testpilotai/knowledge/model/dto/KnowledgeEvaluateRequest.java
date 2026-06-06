package com.zy.testpilotai.knowledge.model.dto;

import lombok.Data;

@Data
public class KnowledgeEvaluateRequest {

    /**
     * 项目 ID。
     */
    private Long projectId;

    /**
     * 版本号。
     */
    private String versionNo;

    /**
     * 模块编码，可为空。
     */
    private String moduleCode;

    /**
     * 可选查询文本。
     * 如果传了，会检查知识库内容是否能被关键词命中。
     */
    private String queryText;

    /**
     * 过短 Chunk 阈值。
     * 默认 80。
     */
    private Integer minChunkLength;

    /**
     * 过长 Chunk 阈值。
     * 默认 3000。
     */
    private Integer maxChunkLength;
}