package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class MissingCaseCompleteResultVO {

    /**
     * 原始任务 ID
     */
    private String taskId;

    /**
     * 使用的评审任务 ID
     */
    private String reviewTaskId;

    /**
     * 新增补全用例数量
     */
    private Integer addedCaseCount;

    /**
     * 新增补全用例列表
     */
    private List<TestCaseVO> addedCases;

    /**
     * 模型原始输出
     */
    private String rawModelOutput;
}