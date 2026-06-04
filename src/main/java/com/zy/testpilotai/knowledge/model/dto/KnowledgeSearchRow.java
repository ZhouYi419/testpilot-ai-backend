package com.zy.testpilotai.knowledge.model.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeSearchRow {

    /**
     * Chunk ID
     */
    private Long id;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 文档 ID
     */
    private Long documentId;

    /**
     * 版本号
     */
    private String versionNo;

    /**
     * 模块编码
     */
    private String moduleCode;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 父 Chunk ID
     */
    private Long parentChunkId;

    /**
     * Chunk 类型
     */
    private String chunkType;

    /**
     * 章节标题
     */
    private String sectionTitle;

    /**
     * Chunk 顺序
     */
    private Integer chunkIndex;

    /**
     * 变更类型
     */
    private String changeType;

    /**
     * Chunk 内容
     */
    private String content;

    /**
     * 预估 token 数
     */
    private Integer tokenCount;

    /**
     * 向量 ID
     */
    private String vectorId;

    /**
     * 元数据 JSON 字符串
     */
    private String metadata;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 向量化状态
     */
    private String embeddingStatus;

    /**
     * Embedding 模型
     */
    private String embeddingModel;

    /**
     * 向量生成时间
     */
    private LocalDateTime embeddedTime;

    /**
     * 相似度分数
     * score 越接近 1，表示越相似
     */
    private Double score;
}