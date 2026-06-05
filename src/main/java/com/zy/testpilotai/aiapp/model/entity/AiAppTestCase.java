package com.zy.testpilotai.aiapp.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.LocalDateTime;

@Data
@TableName(value = "ai_app_test_case", autoResultMap = true)
public class AiAppTestCase {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属任务 ID
     */
    private String taskId;

    /**
     * AI 应用类型
     */
    private String appType;

    /**
     * 测试维度
     */
    private String testDimension;

    /**
     * 用例标题
     */
    private String caseTitle;

    /**
     * 优先级：P0 / P1 / P2 / P3
     */
    private String priority;

    /**
     * 攻击 Prompt 或测试输入 Prompt
     */
    private String attackPrompt;

    /**
     * 输入数据 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String inputData;

    /**
     * 前置条件
     */
    private String precondition;

    /**
     * 测试步骤 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String steps;

    /**
     * 预期行为
     */
    private String expectedBehavior;

    /**
     * 通过标准
     */
    private String passCriteria;

    /**
     * 评估方式
     */
    private String evaluationMethod;

    /**
     * 风险等级：HIGH / MEDIUM / LOW
     */
    private String riskLevel;

    /**
     * 自动化建议
     */
    private String automationSuggestion;

    /**
     * 来源引用 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String sourceReferences;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}