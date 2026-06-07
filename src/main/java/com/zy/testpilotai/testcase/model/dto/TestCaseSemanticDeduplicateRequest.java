package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseSemanticDeduplicateRequest {

    /**
     * 源任务 ID。
     * 常见场景：对某次 AI 生成的用例做语义去重。
     */
    private String taskId;

    /**
     * 项目 ID。
     */
    private Long projectId;

    /**
     * 版本号。
     */
    private String versionNo;

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 对比范围：
     * TASK：同任务内去重
     * VERSION：同项目同版本内去重
     * PROJECT：同项目内去重
     * CROSS_VERSION：同项目同模块跨版本去重
     */
    private String compareScope;

    /**
     * 相似度阈值。
     * 默认 0.85。
     */
    private Double threshold;

    /**
     * 每条用例最多返回多少个相似候选。
     * 默认 5。
     */
    private Integer topK;

    /**
     * 是否重建向量。
     */
    private Boolean rebuildEmbedding;
}