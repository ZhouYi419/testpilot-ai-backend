package com.zy.testpilotai.aiapp.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AiAppTestTaskVO {

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 版本号
     */
    private String versionNo;

    /**
     * 模块编码
     */
    private String moduleCode;

    /**
     * AI 应用类型
     */
    private String appType;

    /**
     * AI 应用说明
     */
    private String appDescription;

    /**
     * 生成目标
     */
    private String generateGoal;

    /**
     * 测试维度 JSON
     */
    private String testDimensions;

    /**
     * 选择的 Skill JSON
     */
    private String selectedSkills;

    /**
     * 状态
     */
    private String status;

    /**
     * 模型原始输出
     */
    private String rawModelOutput;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}