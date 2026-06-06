package com.zy.testpilotai.knowledge.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeEvaluateTaskVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 评估任务全局唯一编号
     */
    private String evaluateTaskId;

    /**
     * 归属的项目ID
     */
    private Long projectId;

    /**
     * 关联的文档版本号
     */
    private String versionNo;

    /**
     * 关联的模块编码
     */
    private String moduleCode;

    /**
     * 测试查询文本 / 评估基准问题
     */
    private String queryText;

    /**
     * 任务执行状态
     */
    private String status;

    /**
     * 知识库整体健康度/综合得分 (0-100分)
     */
    private Double totalScore;

    /**
     * 参与评估的文档总数
     */
    private Integer documentCount;

    /**
     * 参与评估的总切片(Chunk)数量
     */
    private Integer chunkCount;

    /**
     * 父级切片数量
     */
    private Integer parentChunkCount;

    /**
     * 子级切片数量
     */
    private Integer childChunkCount;

    /**
     * 缺失向量化(Embedding)的切片数量
     */
    private Integer embeddingMissingCount;

    /**
     * 缺失模块关联的切片数量
     */
    private Integer moduleMissingCount;

    /**
     * 内容过短的切片数量
     */
    private Integer tooShortChunkCount;

    /**
     * 内容过长的切片数量
     */
    private Integer tooLongChunkCount;

    /**
     * 孤儿切片数量 (Orphan Child Chunk)
     */
    private Integer orphanChildChunkCount;

    /**
     * 综合评估总结/报告
     */
    private String summary;

    /**
     * 任务失败时的错误堆栈/提示信息
     */
    private String errorMessage;

    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;

    /**
     * 任务完成/更新时间
     */
    private LocalDateTime updateTime;
}