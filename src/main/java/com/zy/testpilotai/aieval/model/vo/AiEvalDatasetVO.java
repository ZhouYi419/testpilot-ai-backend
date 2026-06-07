package com.zy.testpilotai.aieval.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiEvalDatasetVO {

    private Long id;

    private String datasetId;

    private Long projectId;

    private String versionNo;

    private String moduleCode;

    private String datasetName;

    private String datasetType;

    private String description;

    private Integer caseCount;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}