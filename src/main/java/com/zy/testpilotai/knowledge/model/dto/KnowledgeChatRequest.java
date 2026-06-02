package com.zy.testpilotai.knowledge.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KnowledgeChatRequest {

    /**
     * 项目 ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * PRD 版本，可选。
     * 如果为空，搜索项目下所有版本。
     */
    private String versionName;

    /**
     * 用户问题
     */
    @NotBlank(message = "问题不能为空")
    private String question;

    /**
     * 召回 chunk 数量
     */
    @Min(value = 1, message = "topK 不能小于1")
    @Max(value = 10, message = "topK 不能大于10")
    private Integer topK = 5;
}