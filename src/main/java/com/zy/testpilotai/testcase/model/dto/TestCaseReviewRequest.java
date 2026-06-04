package com.zy.testpilotai.testcase.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TestCaseReviewRequest {

    /**
     * 要评审的测试用例生成任务 ID
     */
    @NotBlank(message = "测试用例生成任务ID不能为空")
    private String taskId;
}