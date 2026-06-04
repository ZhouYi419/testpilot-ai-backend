package com.zy.testpilotai.knowledge.model.dto;

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
     * 用户查询内容
     */
    @NotBlank(message = "查询内容不能为空")
    private String query;

    /**
     * 版本号
     * 可选，不传则不限制版本
     */
    private String versionNo;

    /**
     * 模块编码
     * 可选，不传则不限制模块
     */
    private String moduleCode;

    /**
     * 返回结果数量
     * 默认取 5 条
     */
    private Integer topK = 5;
}