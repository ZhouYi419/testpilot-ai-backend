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
@TableName(value = "agent_execution_log", autoResultMap = true)
public class AgentExecutionLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Agent 任务 ID。
     */
    private String agentTaskId;

    /**
     * 步骤序号。
     */
    private Integer stepIndex;

    /**
     * 步骤类型。
     */
    private String stepType;

    /**
     * 步骤名称。
     */
    private String stepName;

    /**
     * 日志级别：INFO / WARN / ERROR。
     */
    private String logLevel;

    /**
     * 事件类型。
     */
    private String eventType;

    /**
     * 日志消息。
     */
    private String message;

    /**
     * 输入快照 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String inputSnapshot;

    /**
     * 输出快照 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String outputSnapshot;

    /**
     * 错误信息。
     */
    private String errorMessage;

    /**
     * 错误堆栈。
     */
    private String errorStack;

    /**
     * 模型供应商。
     */
    private String modelProvider;

    /**
     * 模型名称。
     */
    private String modelName;

    /**
     * 输入 token。
     */
    private Integer promptTokens;

    /**
     * 输出 token。
     */
    private Integer completionTokens;

    /**
     * 总 token。
     */
    private Integer totalTokens;

    /**
     * 耗时，毫秒。
     */
    private Long durationMs;

    private LocalDateTime createTime;
}