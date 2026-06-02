package com.zy.testpilotai.knowledge.model.vo;

import lombok.Data;

@Data
public class KnowledgeSearchResultVO {

    private Long chunkId;

    private Long projectId;

    private Long documentId;

    private String versionName;

    private Integer chunkIndex;

    private String title;

    private String content;

    /**
     * cosine distance，越小越相似
     */
    private Double distance;

    /**
     * 简单换算后的相似度：1 - distance
     */
    private Double similarity;

    private String metadata;
}