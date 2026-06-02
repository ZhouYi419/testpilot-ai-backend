package com.zy.testpilotai.knowledge.model.vo;

import lombok.Data;

@Data
public class KnowledgeReferenceVO {

    private Long chunkId;

    private Long projectId;

    private Long documentId;

    private String versionName;

    private Integer chunkIndex;

    private String title;

    private String content;

    private Double similarity;
}