package com.zy.testpilotai.rageval.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RagEvalQuestionVO {

    private Long id;

    private String questionId;

    private String datasetId;

    private String questionText;

    private String standardAnswer;

    private String expectedKeywords;

    private String expectedChunkIds;

    private String expectedDocumentIds;

    private String expectedModuleCode;

    private String expectedVersionNo;

    private String difficulty;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}