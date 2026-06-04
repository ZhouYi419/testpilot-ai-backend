package com.zy.testpilotai.testcase.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class TestCaseGenerateRequest {

    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 版本号，可选。
     */
    private String versionNo;

    /**
     * 模块编码，可选。
     */
    private String moduleCode;

    /**
     * 生成目标。
     */
    @NotBlank(message = "生成目标不能为空")
    private String generateGoal;

    /**
     * 生成类型。
     * FULL：全量
     * MODULE：模块
     * INCREMENTAL：增量，后续扩展
     */
    private String generateType = "FULL";

    /**
     * 选择的 Skill。
     */
    private List<String> selectedSkills;

    /**
     * RAG 召回数量。
     */
    private Integer topK = 5;
}