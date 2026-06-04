package com.zy.testpilotai.requirement.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeImpactAnalyzeRequest {

    /**
     * 项目 ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 基线版本号。
     * 例如：v1.0
     */
    @NotBlank(message = "基线版本号不能为空")
    private String baseVersionNo;

    /**
     * 目标版本号。
     * 例如：v1.1
     */
    @NotBlank(message = "目标版本号不能为空")
    private String targetVersionNo;

    /**
     * 新需求内容。
     */
    @NotBlank(message = "新需求内容不能为空")
    private String newRequirement;

    /**
     * RAG 召回数量。
     */
    private Integer topK = 8;
}