package com.zy.testpilotai.document.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.LocalDateTime;

@Data
@TableName(value = "document_chunk", autoResultMap = true)
public class DocumentChunk {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 所属项目 ID
     */
    private Long projectId;

    /**
     * 所属文档 ID
     */
    private Long documentId;

    /**
     * 文档版本号，例如 v1.0、v1.1
     */
    private String versionNo;

    /**
     * 功能模块编码
     */
    private String moduleCode;

    /**
     * 功能模块名称
     */
    private String moduleName;

    /**
     * 父 Chunk ID
     * Child Chunk 会指向 Parent Chunk
     */
    private Long parentChunkId;

    /**
     * Chunk 类型
     * PARENT：父块
     * CHILD：子块
     */
    private String chunkType;

    /**
     * 所属章节标题
     */
    private String sectionTitle;

    /**
     * Chunk 顺序
     */
    private Integer chunkIndex;

    /**
     * 变更类型
     * NEW / MODIFIED / DELETED / UNKNOWN
     */
    private String changeType;

    /**
     * Chunk 文本内容
     */
    private String content;

    /**
     * 预估 token 数
     */
    private Integer tokenCount;

    /**
     * 外部向量 ID
     */
    private String vectorId;

    /**
     * Chunk 元数据
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String metadata;

    private LocalDateTime createTime;

    /**
     * 向量生成状态
     * PENDING：待生成
     * DONE：已生成
     * FAILED：生成失败
     */
    private String embeddingStatus;

    /**
     * 使用的 Embedding 模型名称
     */
    private String embeddingModel;

    /**
     * 向量生成时间
     */
    private LocalDateTime embeddedTime;
}