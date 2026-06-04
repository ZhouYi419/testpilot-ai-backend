package com.zy.testpilotai.knowledge.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KnowledgeSearchResultVO {

    /**
     * 召回的 Child Chunk ID
     */
    private Long chunkId;

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
     * Child Chunk 所属章节标题
     */
    private String sectionTitle;

    /**
     * Child Chunk 内容
     */
    private String content;

    /**
     * 相似度分数
     * 越接近 1 越相似。
     */
    private Double score;

    /**
     * 元数据 JSON
     */
    private String metadata;

    /**
     * 向量化模型
     */
    private String embeddingModel;

    /**
     * 向量生成时间
     */
    private LocalDateTime embeddedTime;

    /**
     * Parent Chunk ID。
     * 用于追溯完整上下文。
     */
    private Long parentChunkId;

    /**
     * Parent Chunk 章节标题。
     */
    private String parentSectionTitle;

    /**
     * Parent Chunk 内容。
     */
    private String parentContent;
}