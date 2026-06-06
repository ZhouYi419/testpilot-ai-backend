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
@TableName(value = "rag_eval_result", autoResultMap = true)
public class RagEvalResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 运行任务业务 ID。
     */
    private String runId;

    /**
     * 问题业务 ID。
     */
    private String questionId;

    /**
     * 问题文本快照。
     */
    private String questionText;

    /**
     * 标准答案快照。
     */
    private String standardAnswer;

    /**
     * 期望关键词快照。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String expectedKeywords;

    /**
     * 检索上下文 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String retrievedContext;

    /**
     * 是否命中：1 是，0 否。
     */
    private Integer hit;

    /**
     * 首次命中排名，从 1 开始。
     */
    private Integer hitRank;

    /**
     * 是否来源命中：1 是，0 否。
     */
    private Integer sourceHit;

    /**
     * 命中关键词 JSON 数组。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String matchedKeywords;

    /**
     * 得分。
     */
    private Double score;

    /**
     * 评估说明。
     */
    private String evaluationMessage;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}