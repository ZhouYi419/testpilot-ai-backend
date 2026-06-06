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
@TableName(value = "knowledge_evaluate_task", autoResultMap = true)
public class KnowledgeEvaluateTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评估任务业务 ID。
     */
    private String evaluateTaskId;

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
     * 评估查询文本。
     */
    private String queryText;

    /**
     * 状态：RUNNING / SUCCESS / FAILED。
     */
    private String status;

    /**
     * 总评分。
     */
    private Double totalScore;

    /**
     * 文档数量。
     */
    private Integer documentCount;

    /**
     * Chunk 总数。
     */
    private Integer chunkCount;

    /**
     * Parent Chunk 数量。
     */
    private Integer parentChunkCount;

    /**
     * Child Chunk 数量。
     */
    private Integer childChunkCount;

    /**
     * Embedding 缺失数量。
     */
    private Integer embeddingMissingCount;

    /**
     * 模块缺失数量。
     */
    private Integer moduleMissingCount;

    /**
     * 过短 Chunk 数量。
     */
    private Integer tooShortChunkCount;

    /**
     * 过长 Chunk 数量。
     */
    private Integer tooLongChunkCount;

    /**
     * 孤儿 Child Chunk 数量。
     */
    private Integer orphanChildChunkCount;

    /**
     * 评估摘要 JSON。
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