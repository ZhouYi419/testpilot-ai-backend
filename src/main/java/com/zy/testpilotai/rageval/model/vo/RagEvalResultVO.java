package com.zy.testpilotai.rageval.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RagEvalResultVO {

    private Long id;

    private String runId;

    private String questionId;

    private String questionText;

    private String standardAnswer;

    private String expectedKeywords;

    private String retrievedContext;

    private Integer hit;

    private Integer hitRank;

    private Integer sourceHit;

    private String matchedKeywords;

    private Double score;

    private String evaluationMessage;

    private LocalDateTime createTime;
}