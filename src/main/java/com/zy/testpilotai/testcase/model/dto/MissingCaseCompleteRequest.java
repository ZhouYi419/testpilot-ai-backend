package com.zy.testpilotai.testcase.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MissingCaseCompleteRequest {

    /**
     * 原始测试用例生成任务 ID
     */
    @NotBlank(message = "测试用例生成任务ID不能为空")
    private String taskId;

    /**
     * 质量评审任务 ID。
     * 可选，不传则默认使用该 taskId 最近一次成功评审结果。
     */
    private String reviewTaskId;

    /**
     * 知识库召回数量。
     */
    private Integer topK = 5;
}