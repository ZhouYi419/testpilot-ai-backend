package com.zy.testpilotai.document.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DocumentChunkVO {

    private Long id;

    /**
     * 归属的项目ID
     */
    private Long projectId;

    /**
     * 关联的文档ID
     */
    private Long documentId;

    /**
     * 文档版本号
     */
    private String versionNo;

    /**
     * 关联的模块编码
     */
    private String moduleCode;

    /**
     * 关联的模块名称
     */
    private String moduleName;

    /**
     * 父切片ID
     */
    private Long parentChunkId;

    /**
     * 切片类型
     */
    private String chunkType;

    /**
     * 所在章节的标题
     */
    private String sectionTitle;

    /**
     * 切片排序索引
     */
    private Integer chunkIndex;

    /**
     * 变更类型
     */
    private String changeType;

    /**
     * 切片的纯文本内容
     */
    private String content;

    /**
     * 内容对应的 Token 数量
     */
    private Integer tokenCount;

    /**
     * 向量库中的唯一ID
     */
    private String vectorId;

    /**
     * 扩展元数据 (JSON 字符串)
     */
    private String metadata;

    private LocalDateTime createTime;
}