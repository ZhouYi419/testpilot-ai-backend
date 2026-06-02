package com.zy.testpilotai.knowledge.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeChunkEntity {

    private Long id;

    private Long projectId;

    private Long documentId;

    private String versionName;

    private Integer chunkIndex;

    private String title;

    private String content;

    private Integer tokenCount;

    private String metadata;

    /**
     * pgvector 字符串格式，例如：[0.1,0.2,0.3]
     */
    private String embedding;

    private String embeddingModel;

    private String embeddingStatus;

    private String embeddingErrorMessage;

    private Double distance;

    private Boolean enabled;

    private Boolean deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}