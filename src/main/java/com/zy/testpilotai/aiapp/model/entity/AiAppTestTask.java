package com.zy.testpilotai.aiapp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.LocalDateTime;

@Data
@TableName(value = "ai_app_test_task", autoResultMap = true)
public class AiAppTestTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * AI 应用测试任务 ID
     */
    private String taskId;

    /**
     * 可选：项目 ID
     */
    private Long projectId;

    /**
     * 可选：版本号
     */
    private String versionNo;

    /**
     * 可选：模块编码
     */
    private String moduleCode;

    /**
     * AI 应用类型：LLM / RAG / AGENT / PROMPT / AI_APP
     */
    private String appType;

    /**
     * AI 应用描述
     */
    private String appDescription;

    /**
     * 生成目标
     */
    private String generateGoal;

    /**
     * 测试维度 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String testDimensions;

    /**
     * 选择的 Skill JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String selectedSkills;

    /**
     * 任务状态：RUNNING / SUCCESS / FAILED
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

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}