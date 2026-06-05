package com.zy.testpilotai.aiapp.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class AiAppTestGenerateRequest {

    /**
     * 可选：项目 ID。
     */
    private Long projectId;

    /**
     * 可选：版本号。
     */
    private String versionNo;

    /**
     * 可选：模块编码。
     */
    private String moduleCode;

    /**
     * AI 应用类型。
     */
    @NotBlank(message = "AI应用类型不能为空")
    private String appType;

    /**
     * AI 应用说明。
     */
    @NotBlank(message = "AI应用说明不能为空")
    private String appDescription;

    /**
     * 生成目标。
     */
    private String generateGoal;

    /**
     * 测试维度。
     */
    private List<String> testDimensions;

    /**
     * 选择的 Skill。
     */
    private List<String> selectedSkills;

    /**
     * 知识库召回数量。
     */
    private Integer topK = 5;
}