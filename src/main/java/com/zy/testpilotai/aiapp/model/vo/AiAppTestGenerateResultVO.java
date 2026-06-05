package com.zy.testpilotai.aiapp.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class AiAppTestGenerateResultVO {

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 生成用例数量
     */
    private Integer caseCount;

    /**
     * 生成的 AI 应用测试用例
     */
    private List<AiAppTestCaseVO> testCases;

    /**
     * 模型原始输出
     */
    private String rawModelOutput;
}