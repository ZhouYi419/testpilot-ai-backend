package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseEmbeddingBuildRequest {

    /**
     * 任务 ID。
     * 如果传了 taskId，则只构建该任务下的用例向量。
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
     * 是否强制重建。
     * true：即使内容未变化也重新生成向量。
     * false：内容未变化则跳过。
     */
    private Boolean rebuild;
}