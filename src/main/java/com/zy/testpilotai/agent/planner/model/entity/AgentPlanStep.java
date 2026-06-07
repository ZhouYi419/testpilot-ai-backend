package com.zy.testpilotai.agent.planner.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;

import java.time.LocalDateTime;

@Data
@TableName(value = "agent_plan_step", autoResultMap = true)
public class AgentPlanStep {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Agent 计划任务业务 ID。
     */
    private String planTaskId;

    /**
     * 步骤序号。
     */
    private Integer stepIndex;

    /**
     * 工具名称。
     */
    private String toolName;

    /**
     * 步骤名称。
     */
    private String stepName;

    /**
     * 步骤目标。
     */
    private String stepGoal;

    /**
     * 工具入参 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String inputParams;

    /**
     * 状态：
     * PENDING / RUNNING / SUCCESS / FAILED / SKIPPED。
     */
    private String status;

    /**
     * 工具输出 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String outputJson;

    /**
     * 错误信息。
     */
    private String errorMessage;

    /**
     * 重试次数。
     */
    private Integer retryCount;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}