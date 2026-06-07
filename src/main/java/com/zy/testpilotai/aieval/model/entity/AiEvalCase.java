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
@TableName(value = "ai_eval_case", autoResultMap = true)
public class AiEvalCase {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 样本业务 ID。
     */
    private String caseId;

    /**
     * 数据集业务 ID。
     */
    private String datasetId;

    /**
     * 测试类型：
     * RAG_QA / RAG_SOURCE_CITATION / HALLUCINATION / PROMPT_INJECTION /
     * KNOWLEDGE_ACCESS_CONTROL / AGENT_TOOL_CALL / OUTPUT_FORMAT /
     * CONSISTENCY / REFUSAL。
     */
    private String caseType;

    /**
     * 测试维度：
     * ACCURACY / SECURITY / STABILITY / COST / PERFORMANCE / FORMAT / TOOL_USE。
     */
    private String testDimension;

    /**
     * 样本名称。
     */
    private String caseName;

    /**
     * 用户输入。
     */
    private String inputText;

    /**
     * 上下文。
     */
    private String contextText;

    /**
     * 期望行为。
     */
    private String expectedBehavior;

    /**
     * 标准答案。
     */
    private String expectedAnswer;

    /**
     * 期望关键词 JSON 数组。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String expectedKeywords;

    /**
     * 禁止出现的关键词 JSON 数组。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String forbiddenKeywords;

    /**
     * 期望工具名称。
     */
    private String expectedToolName;

    /**
     * 期望来源 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String expectedSources;

    /**
     * 期望输出格式：
     * JSON / MARKDOWN / TEXT。
     */
    private String expectedOutputFormat;

    /**
     * 风险等级：
     * LOW / MEDIUM / HIGH / CRITICAL。
     */
    private String riskLevel;

    /**
     * 标签 JSON 数组。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String tags;

    /**
     * 状态：
     * ACTIVE / DELETED。
     */
    private String status;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}