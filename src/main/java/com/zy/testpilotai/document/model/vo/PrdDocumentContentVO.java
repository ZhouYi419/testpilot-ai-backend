package com.zy.testpilotai.document.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PrdDocumentContentVO {

    private Long id;

    private Long projectId;

    private String versionName;

    private String originalFileName;

    private String parseStatus;

    private String parsedContent;

    private String errorMessage;

    private LocalDateTime updatedAt;
}