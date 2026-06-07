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
@TableName(value = "agent_plan_task", autoResultMap = true)
public class AgentPlanTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Agent 计划任务业务 ID。
     */
    private String planTaskId;

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
     * 用户目标。
     */
    private String userGoal;

    /**
     * 规划模式：
     * LLM / TEMPLATE。
     */
    private String planningMode;

    /**
     * 允许使用的工具 JSON 数组。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String allowedTools;

    /**
     * 原始模型输出。
     */
    private String rawModelOutput;

    /**
     * 最终计划 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String planJson;

    /**
     * 状态：
     * PLANNING / WAITING_CONFIRM / RUNNING / SUCCESS / FAILED / CANCELLED。
     */
    private String status;

    /**
     * 是否已确认执行：
     * 0 否，1 是。
     */
    private Integer approved;

    /**
     * 当前步骤序号。
     */
    private Integer currentStepIndex;

    /**
     * 总步骤数。
     */
    private Integer totalStepCount;

    /**
     * 成功步骤数。
     */
    private Integer successStepCount;

    /**
     * 失败步骤数。
     */
    private Integer failedStepCount;

    /**
     * 最终结果 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String finalResult;

    /**
     * 错误信息。
     */
    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}