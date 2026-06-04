package com.zy.testpilotai.knowledge.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeBuildTaskVO {

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 文档 ID
     */
    private Long documentId;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 总数
     */
    private Integer totalChunks;

    /**
     * 成功数
     */
    private Integer successChunks;

    /**
     * 失败数
     */
    private Integer failChunks;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}