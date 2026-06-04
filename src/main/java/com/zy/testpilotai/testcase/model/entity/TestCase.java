package com.zy.testpilotai.testcase.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.LocalDateTime;

@Data
@TableName(value = "test_case", autoResultMap = true)
public class TestCase {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属生成任务 ID
     */
    private String taskId;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 版本号
     */
    private String versionNo;

    /**
     * 模块编码
     */
    private String moduleCode;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 用例标题
     */
    private String caseTitle;

    /**
     * 用例类型
     * 功能测试 / 异常测试 / 边界测试 / 安全测试 / 回归测试
     */
    private String caseType;

    /**
     * 优先级
     * P0 / P1 / P2 / P3
     */
    private String priority;

    /**
     * 前置条件
     */
    private String precondition;

    /**
     * 测试步骤，JSONB 数组
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String steps;

    /**
     * 预期结果
     */
    private String expectedResult;

    /**
     * 测试数据，JSONB 对象
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String testData;

    /**
     * 来源引用，JSONB 数组
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String sourceReferences;

    /**
     * 风险点
     */
    private String riskPoint;

    /**
     * 自动化建议
     */
    private String automationSuggestion;

    /**
     * 单条用例质量评分
     */
    private Double qualityScore;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /**
     * 重复状态
     * NORMAL：正常用例
     * DUPLICATE：重复用例
     */
    private String duplicateStatus;

    /**
     * 如果当前用例是重复用例，则记录被重复的用例 ID
     */
    private Long duplicateOfCaseId;

    /**
     * 重复相似度分数
     */
    private Double duplicateScore;

    /**
     * 重复原因
     */
    private String duplicateReason;

    /**
     * 用例来源类型
     * AI_GENERATED：AI 首次生成
     * AI_COMPLETED：AI 补全生成
     * MANUAL：人工新增
     */
    private String sourceType;
}