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
@TableName(value = "ai_eval_result", autoResultMap = true)
public class AiEvalResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 运行任务业务 ID。
     */
    private String runId;

    /**
     * 样本业务 ID。
     */
    private String caseId;

    /**
     * 数据集业务 ID。
     */
    private String datasetId;

    /**
     * 应用配置 ID。
     */
    private String appConfigId;

    /**
     * 测试类型。
     */
    private String caseType;

    /**
     * 测试维度。
     */
    private String testDimension;

    /**
     * 样本名称。
     */
    private String caseName;

    /**
     * 输入文本。
     */
    private String inputText;

    /**
     * 请求体。
     */
    private String requestPayload;

    /**
     * HTTP 状态码。
     */
    private Integer httpStatus;

    /**
     * 响应体。
     */
    private String responseBody;

    /**
     * 模型输出。
     */
    private String modelOutput;

    /**
     * 是否通过。
     */
    private Integer passed;

    /**
     * 准确性是否通过。
     */
    private Integer accuracyPass;

    /**
     * 安全性是否通过。
     */
    private Integer securityPass;

    /**
     * 格式是否通过。
     */
    private Integer formatPass;

    /**
     * 工具调用是否通过。
     */
    private Integer toolCallPass;

    /**
     * 来源引用是否通过。
     */
    private Integer sourcePass;

    /**
     * 是否命中期望关键词。
     */
    private Integer expectedKeywordHit;

    /**
     * 是否出现禁止关键词。
     */
    private Integer forbiddenKeywordHit;

    /**
     * 命中的期望关键词 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String matchedExpectedKeywords;

    /**
     * 命中的禁止关键词 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String matchedForbiddenKeywords;

    /**
     * 得分。
     */
    private Double score;

    /**
     * 响应耗时。
     */
    private Long latencyMs;

    /**
     * 评估说明。
     */
    private String evaluationMessage;

    /**
     * 错误信息。
     */
    private String errorMessage;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}