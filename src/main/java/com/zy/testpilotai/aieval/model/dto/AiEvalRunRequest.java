package com.zy.testpilotai.aieval.model.dto;

import lombok.Data;

@Data
public class AiEvalRunRequest {

    /**
     * 数据集业务 ID。
     */
    private String datasetId;

    /**
     * 待测 AI 应用配置 ID。
     */
    private String appConfigId;
}