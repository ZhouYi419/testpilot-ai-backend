package com.zy.testpilotai.agent.model.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AgentTaskVO {

    /**
     * Agent 任务 ID
     */
    private String agentTaskId;

    /**
     * 工作流类型
     */
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
     * 用户目标
     */
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
     * 选择的 Skill JSON
     */
    private String selectedSkills;

    /**
     * 状态
     */
    private String status;

    /**
     * 最终结果 JSON
     */
    private String finalResult;

    /**
     * 影响分析任务 ID
     */
    private String analysisTaskId;

    /**
     * 测试用例生成任务 ID
     */
    private String testcaseTaskId;

    /**
     * AI 应用测试任务 ID
     */
    private String aiAppTaskId;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行步骤
     */
    private List<AgentTaskStepVO> steps;

    /**
     * 执行模式
     */
    private String executionMode;

    /**
     * 当前执行步骤
     */
    private Integer currentStepIndex;

    /**
     * 是否请求取消
     */
    private Integer cancelRequested;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 从第几步恢复
     */
    private Integer resumeFromStep;

    /**
     * RAG 召回数量。
     */
    private Integer topK;

    /**
     * 是否自动质量评审。
     */
    private Integer autoReview;

    /**
     * 是否自动补全缺失用例。
     */
    private Integer autoCompleteMissing;

    /**
     * 是否自动去重。
     */
    private Integer autoDeduplicate;

    /**
     * 去重阈值。
     */
    private Double deduplicateThreshold;

    /**
     * AI 应用测试维度 JSON。
     */
    private String testDimensions;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}