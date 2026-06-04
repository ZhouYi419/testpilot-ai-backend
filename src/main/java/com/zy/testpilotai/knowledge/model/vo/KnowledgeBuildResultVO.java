package com.zy.testpilotai.knowledge.model.vo;

import lombok.Data;

@Data
public class KnowledgeBuildResultVO {

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 文档 ID
     */
    private Long documentId;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 总 Chunk 数
     */
    private Integer totalChunks;

    /**
     * 成功数量
     */
    private Integer successChunks;

    /**
     * 失败数量
     */
    private Integer failChunks;

    /**
     * 错误信息
     */
    private String errorMessage;
}