package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseDeduplicateRequest {

    /**
     * 任务 ID。
     */
    private String taskId;

    /**
     * 项目 ID。
     */
    private Long projectId;

    /**
     * 版本号。
     */
    private String versionNo;

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 相似度阈值。
     * 默认 0.85。
     * 分数越高，表示判断越严格。
     */
    private Double threshold = 0.85;
}