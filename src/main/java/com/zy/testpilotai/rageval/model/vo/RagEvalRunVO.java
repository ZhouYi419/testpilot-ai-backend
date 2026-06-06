package com.zy.testpilotai.rageval.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RagEvalRunVO {

    private Long id;

    private String runId;

    private String datasetId;

    private Long projectId;

    private String versionNo;

    private String moduleCode;

    private Integer topK;

    private String status;

    private Integer totalQuestions;

    private Integer hitCount;

    private Double recallAtK;

    private Double mrr;

    private Double sourceHitRate;

    private Double avgScore;

    private String summary;

    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}