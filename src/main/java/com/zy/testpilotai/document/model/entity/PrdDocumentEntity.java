package com.zy.testpilotai.document.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PrdDocumentEntity {

    private Long id;

    private Long projectId;

    private String versionName;

    private String fileName;

    private String originalFileName;

    private String filePath;

    private String fileType;

    private Long fileSize;

    /**
     * PENDING / PARSING / PARSED / FAILED
     */
    private String parseStatus;

    /**
     * PENDING / INDEXING / INDEXED / FAILED
     */
    private String indexStatus;

    private String parsedContent;

    private String contentHash;

    private String description;

    private String errorMessage;

    private Boolean deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}