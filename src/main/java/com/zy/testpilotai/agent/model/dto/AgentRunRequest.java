package com.zy.testpilotai.agent.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class AgentRunRequest {

    @NotBlank(message = "工作流类型不能为空")
    private String workflowType;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 基线版本号
     */
    private String baseVersionNo;

    /**
     * 目标版本号
     */
    private String targetVersionNo;

    /**
     * 模块编码
     */
    private String moduleCode;

    /**
     * 用户输入目标
     */
    @NotBlank(message = "用户目标不能为空")
    private String userGoal;

    /**
     * 新需求内容
     */
    private String newRequirement;

    /**
     * AI 应用类型
     */
    private String appType;

    /**
     * AI 应用说明
     */
    private String appDescription;

    /**
     * 测试维度
     */
    private List<String> testDimensions;

    /**
     * 选择的 Skill
     */
    private List<String> selectedSkills;

    /**
     * RAG 召回数量
     */
    private Integer topK = 8;

    /**
     * 是否自动质量评审
     */
    private Boolean autoReview = true;

    /**
     * 是否自动补全缺失用例
     */
    private Boolean autoCompleteMissing = true;

    /**
     * 是否自动去重
     */
    private Boolean autoDeduplicate = true;

    /**
     * 去重阈值
     */
    private Double deduplicateThreshold = 0.85;
}