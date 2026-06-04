package com.zy.testpilotai.testcase.model.excel;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class TestCaseExcelRow {

    /**
     * 用例 ID
     */
    @ExcelProperty("用例ID")
    private Long id;

    /**
     * 任务 ID
     */
    @ExcelProperty("任务ID")
    private String taskId;

    /**
     * 项目 ID
     */
    @ExcelProperty("项目ID")
    private Long projectId;

    /**
     * 版本号
     */
    @ExcelProperty("版本号")
    private String versionNo;

    /**
     * 模块编码
     */
    @ExcelProperty("模块编码")
    private String moduleCode;

    /**
     * 模块名称
     */
    @ExcelProperty("模块名称")
    private String moduleName;

    /**
     * 用例标题
     */
    @ExcelProperty("用例标题")
    private String caseTitle;

    /**
     * 用例类型
     */
    @ExcelProperty("用例类型")
    private String caseType;

    /**
     * 优先级
     */
    @ExcelProperty("优先级")
    private String priority;

    /**
     * 前置条件
     */
    @ExcelProperty("前置条件")
    private String precondition;

    /**
     * 测试步骤
     */
    @ExcelProperty("测试步骤")
    private String steps;

    /**
     * 预期结果
     */
    @ExcelProperty("预期结果")
    private String expectedResult;

    /**
     * 测试数据
     */
    @ExcelProperty("测试数据")
    private String testData;

    /**
     * 来源引用
     */
    @ExcelProperty("来源引用")
    private String sourceReferences;

    /**
     * 风险点
     */
    @ExcelProperty("风险点")
    private String riskPoint;

    /**
     * 自动化建议
     */
    @ExcelProperty("自动化建议")
    private String automationSuggestion;

    /**
     * 质量评分
     */
    @ExcelProperty("质量评分")
    private Double qualityScore;

    /**
     * 重复状态
     */
    @ExcelProperty("重复状态")
    private String duplicateStatus;

    /**
     * 重复原因
     */
    @ExcelProperty("重复原因")
    private String duplicateReason;

    /**
     * 来源类型
     */
    @ExcelProperty("来源类型")
    private String sourceType;
}