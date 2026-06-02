package com.zy.testpilotai.knowledge.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class KnowledgeSearchRequest {

    /**
     * 项目 ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * PRD 版本，可选。
     * 如果为空，则搜索该项目下所有版本。
     */
    private String versionName;

    /**
     * 用户查询内容
     */
    @NotBlank(message = "查询内容不能为空")
    private String query;

    /**
     * 返回数量
     */
    @Min(value = 1, message = "topK 不能小于1")
    @Max(value = 20, message = "topK 不能大于20")
    private Integer topK = 5;

    /**
     * 最低相似度，可选。
     */
    private Double minSimilarity;
}