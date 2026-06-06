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
@TableName(value = "rag_eval_question", autoResultMap = true)
public class RagEvalQuestion {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 问题业务 ID。
     */
    private String questionId;

    /**
     * 评测集业务 ID。
     */
    private String datasetId;

    /**
     * 问题文本。
     */
    private String questionText;

    /**
     * 标准答案。
     */
    private String standardAnswer;

    /**
     * 期望关键词 JSON 数组。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String expectedKeywords;

    /**
     * 期望 Chunk ID JSON 数组。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String expectedChunkIds;

    /**
     * 期望文档 ID JSON 数组。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String expectedDocumentIds;

    /**
     * 期望模块编码。
     */
    private String expectedModuleCode;

    /**
     * 期望版本号。
     */
    private String expectedVersionNo;

    /**
     * 难度：EASY / MEDIUM / HARD。
     */
    private String difficulty;

    /**
     * 状态：ACTIVE / DELETED。
     */
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}