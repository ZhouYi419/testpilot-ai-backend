package com.zy.testpilotai.knowledge.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeChunkVO {

    private Long id;

    private Long projectId;

    private Long documentId;

    private String versionName;

    private Integer chunkIndex;

    private String title;

    private String content;

    private Integer tokenCount;

    private String chunkType;

    private Long parentChunkId;

    private String sectionPath;

    private String moduleName;

    private String requirementId;

    private Integer startPosition;

    private Integer endPosition;

    private String sourceType;

    private String metadata;

    private String embeddingModel;

    private String embeddingStatus;

    private String embeddingErrorMessage;

    private Boolean enabled;

    private LocalDateTime createdAt;
}