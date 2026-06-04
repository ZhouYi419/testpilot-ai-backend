package com.zy.testpilotai.requirement.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.LocalDateTime;

@Data
@TableName(value = "requirement_change_analysis_task", autoResultMap = true)
public class RequirementChangeAnalysisTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 影响分析任务 ID
     */
    private String analysisTaskId;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 版本号
     */
    private String baseVersionNo;

    /**
     * 目标版本号
     */
    private String targetVersionNo;

    /**
     * 新需求内容
     */
    private String newRequirement;

    /**
     * 任务状态：RUNNING / SUCCESS / FAILED
     */
    private String status;

    /**
     * 影响模块 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String affectedModules;

    /**
     * 相关旧规则 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String relatedOldRules;

    /**
     * 相关历史用例 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String relatedHistoricalCases;

    /**
     * 变更摘要 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String changeSummary;

    /**
     * 风险点 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String riskPoints;

    /**
     * 回归范围 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String regressionScope;

    /**
     * 建议新增测试点 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String suggestedNewTestPoints;

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