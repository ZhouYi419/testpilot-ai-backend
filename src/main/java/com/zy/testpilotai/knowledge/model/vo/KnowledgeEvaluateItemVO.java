package com.zy.testpilotai.knowledge.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeEvaluateItemVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联的评估任务ID
     */
    private String evaluateTaskId;

    /**
     * 评估维度大类
     */
    private String dimension;

    /**
     * 具体指标名称
     */
    private String metricName;

    /**
     * 指标实际测量值/统计结果
     */
    private String metricValue;

    /**
     * 该单项指标的健康状态
     */
    private String status;

    /**
     * 该单项的具体得分或扣分权重
     */
    private Double score;

    /**
     * 发现的具体问题描述
     */
    private String problem;

    /**
     * AI 或系统给出的优化/修复建议
     */
    private String suggestion;

    /**
     * 详细数据快照
     */
    private String detail;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}