package com.zy.testpilotai.agent.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.LocalDateTime;

@Data
@TableName(value = "agent_task", autoResultMap = true)
public class AgentTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Agent 任务 ID
     */
    private String agentTaskId;

    /**
     * 工作流类型：
     * STANDARD_TEST_DESIGN / INCREMENTAL_TEST_DESIGN / AI_APP_TEST_DESIGN
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
     * 用户输入目标
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
     * 选择的 Skill
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String selectedSkills;

    /**
     * Agent 状态
     */
    private String status;

    /**
     * 最终结果 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String finalResult;

    /**
     * 关联影响分析任务 ID
     */
    private String analysisTaskId;

    /**
     * 关联测试用例生成任务 ID
     */
    private String testcaseTaskId;

    /**
     * 关联 AI 应用测试任务 ID
     */
    private String aiAppTaskId;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 执行模式：
     * ASYNC：异步执行
     * SYNC：同步执行
     */
    private String executionMode;

    /**
     * 当前执行到第几步
     */
    private Integer currentStepIndex;

    /**
     * 是否请求取消：
     * 0：未取消
     * 1：已请求取消
     */
    private Integer cancelRequested;

    /**
     * 任务开始时间
     */
    private LocalDateTime startTime;

    /**
     * 任务结束时间
     */
    private LocalDateTime endTime;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 从第几步恢复执行
     */
    private Integer resumeFromStep;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}