package com.zy.testpilotai.rageval.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RagEvalDatasetVO {

    private Long id;

    private String datasetId;

    private Long projectId;

    private String versionNo;

    private String moduleCode;

    private String datasetName;

    private String description;

    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}