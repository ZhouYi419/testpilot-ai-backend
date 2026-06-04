package com.zy.testpilotai.document.model.vo;

import lombok.Data;

@Data
public class PrdParseResultVO {

    /**
     * 关联的文档ID
     */
    private Long documentId;

    /**
     * 文档解析状态
     */
    private String parseStatus;

    /**
     * 解析出的原始文本总长度/总字符数
     */
    private Integer rawTextLength;

    /**
     * 父级切片（Chunk）数量
     */
    private Integer parentChunkCount;

    /**
     * 子级切片（Chunk）数量
     */
    private Integer childChunkCount;

    /**
     * 总切片数量
     */
    private Integer totalChunkCount;
}