package com.zy.testpilotai.rageval.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.LocalDateTime;

@Data
@TableName(value = "rag_eval_run", autoResultMap = true)
public class RagEvalRun {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 运行任务业务 ID。
     */
    private String runId;

    /**
     * 评测集业务 ID。
     */
    private String datasetId;

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
     * 召回数量。
     */
    private Integer topK;

    /**
     * 状态：RUNNING / SUCCESS / FAILED。
     */
    private String status;

    /**
     * 总问题数。
     */
    private Integer totalQuestions;

    /**
     * 命中问题数。
     */
    private Integer hitCount;

    /**
     * Recall@K。
     */
    private Double recallAtK;

    /**
     * MRR。
     */
    private Double mrr;

    /**
     * 来源命中率。
     */
    private Double sourceHitRate;

    /**
     * 平均得分。
     */
    private Double avgScore;

    /**
     * 汇总 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String summary;

    /**
     * 错误信息。
     */
    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}