package com.zy.testpilotai.requirement.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.util.List;

@Data
public class IncrementalTestCaseGenerateRequest {

    /**
     * 影响分析任务 ID。
     */
    @NotBlank(message = "影响分析任务ID不能为空")
    private String analysisTaskId;

    /**
     * 选择的 Skill。
     */
    private List<String> selectedSkills;

    /**
     * RAG 召回数量。
     */
    private Integer topK = 8;
}