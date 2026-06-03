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

    private String chunkType;

    private String sectionPath;

    private String moduleName;

    private String requirementId;

    /**
     * cosine distance，越小越相似
     */
    private Double distance;

    private Double similarity;

    private String metadata;
}