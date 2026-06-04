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
     * 归属的项目ID
     */
    private Long projectId;

    /**
     * 关联的文档ID (对应 prd_document 表的 id)
     */
    private Long documentId;

    /**
     * 文档版本号，随原文档版本走，便于做多版本切片的对比和隔离
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
     * 扩展元数据 (JSON/JSONB 格式)
     * 存储一些不固定的额外信息
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String metadata;

    private LocalDateTime createTime;
}