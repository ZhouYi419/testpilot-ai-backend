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
@TableName(value = "agent_task_step", autoResultMap = true)
public class AgentTaskStep {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Agent 任务 ID
     */
    private String agentTaskId;

    /**
     * 步骤序号
     */
    private Integer stepIndex;

    /**
     * 步骤名称
     */
    private String stepName;

    /**
     * 步骤状态：
     * RUNNING / SUCCESS / FAILED / SKIPPED
     */
    private String status;

    /**
     * 步骤输入 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String input;

    /**
     * 步骤输出 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String output;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 是否可重试：
     * 1：可重试
     * 0：不可重试
     */
    private Integer retryable;

    /**
     * 步骤类型：
     * GENERATE / REVIEW / COMPLETE / DEDUPLICATE / IMPACT_ANALYZE / AI_APP_TEST
     */
    private String stepType;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}