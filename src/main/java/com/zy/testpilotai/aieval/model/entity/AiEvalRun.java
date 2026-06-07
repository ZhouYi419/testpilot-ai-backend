package com.zy.testpilotai.aieval.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.LocalDateTime;

@Data
@TableName(value = "ai_eval_run", autoResultMap = true)
public class AiEvalRun {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 运行任务业务 ID。
     */
    private String runId;

    /**
     * 数据集业务 ID。
     */
    private String datasetId;

    /**
     * 待测应用配置 ID。
     */
    private String appConfigId;

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
     * 状态：
     * RUNNING / SUCCESS / FAILED。
     */
    private String status;

    /**
     * 样本总数。
     */
    private Integer totalCaseCount;

    /**
     * 通过样本数。
     */
    private Integer passedCaseCount;

    /**
     * 失败样本数。
     */
    private Integer failedCaseCount;

    /**
     * 异常数。
     */
    private Integer errorCount;

    /**
     * 平均分。
     */
    private Double avgScore;

    /**
     * 准确性通过率。
     */
    private Double accuracyPassRate;

    /**
     * 安全通过率。
     */
    private Double securityPassRate;

    /**
     * 格式通过率。
     */
    private Double formatPassRate;

    /**
     * 平均响应耗时。
     */
    private Double avgLatencyMs;

    /**
     * Prompt 注入成功数。
     */
    private Integer promptInjectionSuccessCount;

    /**
     * 幻觉风险数。
     */
    private Integer hallucinationCount;

    /**
     * 知识越权风险数。
     */
    private Integer knowledgeLeakCount;

    /**
     * 汇总 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String summary;

    /**
     * 错误信息。
     */
    private String errorMessage;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    /**
     * 开始时间。
     */
    private LocalDateTime startTime;

    /**
     * 结束时间。
     */
    private LocalDateTime endTime;
}