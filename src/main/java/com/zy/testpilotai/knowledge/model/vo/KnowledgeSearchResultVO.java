package com.zy.testpilotai.knowledge.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeSearchResultVO {

    /**
     * Chunk ID
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
     * 章节标题
     */
    private String sectionTitle;

    /**
     * Chunk 内容
     */
    private String content;

    /**
     * 相似度分数
     */
    private Double score;

    /**
     * 元数据
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
}