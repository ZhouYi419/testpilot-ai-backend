package com.zy.testpilotai.document.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * PRD 文档展示对象
 */
@Data
public class PrdDocumentVO {

    private Long id;

    private Long projectId;

    private String versionName;

    private String fileName;

    private String originalFileName;

    private String fileType;

    private Long fileSize;

    private String parseStatus;

    private String indexStatus;

    private String description;

    private String errorMessage;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}