package com.zy.testpilotai.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.LocalDateTime;

@Data
@TableName(value = "knowledge_evaluate_item", autoResultMap = true)
public class KnowledgeEvaluateItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评估任务业务 ID。
     */
    private String evaluateTaskId;

    /**
     * 评估维度。
     */
    private String dimension;

    /**
     * 指标名称。
     */
    private String metricName;

    /**
     * 指标值。
     */
    private String metricValue;

    /**
     * 状态：PASS / WARN / FAIL。
     */
    private String status;

    /**
     * 得分。
     */
    private Double score;

    /**
     * 问题说明。
     */
    private String problem;

    /**
     * 修复建议。
     */
    private String suggestion;

    /**
     * 详情 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String detail;

    private LocalDateTime createTime;
}