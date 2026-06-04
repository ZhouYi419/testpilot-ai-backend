package com.zy.testpilotai.knowledge.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("knowledge_build_task")
public class KnowledgeBuildTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务任务 ID
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
     * 任务状态：
     * PENDING / RUNNING / SUCCESS / FAILED
     */
    private String status;

    /**
     * 总 Chunk 数
     */
    private Integer totalChunks;

    /**
     * 成功向量化 Chunk 数
     */
    private Integer successChunks;

    /**
     * 失败 Chunk 数
     */
    private Integer failChunks;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 任务开始时间
     */
    private LocalDateTime startTime;

    /**
     * 任务结束时间
     */
    private LocalDateTime endTime;

    private LocalDateTime createTime;
}