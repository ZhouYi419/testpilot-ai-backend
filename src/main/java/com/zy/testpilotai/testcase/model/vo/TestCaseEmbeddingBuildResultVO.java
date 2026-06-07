package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;

@Data
public class TestCaseEmbeddingBuildResultVO {

    /**
     * 总用例数量。
     */
    private Integer totalCaseCount;

    /**
     * 成功构建数量。
     */
    private Integer successCount;

    /**
     * 跳过数量。
     */
    private Integer skippedCount;

    /**
     * 失败数量。
     */
    private Integer failedCount;

    /**
     * Embedding 模型名称。
     */
    private String embeddingModel;

    /**
     * 向量维度。
     */
    private Integer embeddingDimension;
}