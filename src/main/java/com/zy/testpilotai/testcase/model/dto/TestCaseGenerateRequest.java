package com.zy.testpilotai.testcase.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class TestCaseGenerateRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * PRD 版本，可选。
     */
    private String versionName;

    /**
     * 模块名称，例如：登录模块、PRD上传模块。
     */
    private String moduleName;

    /**
     * 生成目标，例如：请生成登录模块完整测试用例。
     */
    @NotBlank(message = "生成需求不能为空")
    private String requirementText;

    /**
     * 用例类型，例如：正常流程、异常流程、边界值、安全测试。
     */
    private List<String> caseTypes;

    /**
     * 期望生成数量。
     */
    @Min(value = 1, message = "count 不能小于1")
    @Max(value = 50, message = "count 不能大于50")
    private Integer count = 10;

    /**
     * RAG 召回数量。
     */
    @Min(value = 1, message = "topK 不能小于1")
    @Max(value = 10, message = "topK 不能大于10")
    private Integer topK = 5;
}