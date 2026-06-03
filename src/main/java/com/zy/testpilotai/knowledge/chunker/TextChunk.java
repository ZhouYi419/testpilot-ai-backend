package com.zy.testpilotai.knowledge.chunker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 文本切片结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextChunk {

    private Integer chunkIndex;

    private String title;

    private String content;

    private Integer tokenCount;

    /**
     * SECTION / REQUIREMENT / TABLE / PARAGRAPH
     */
    private String chunkType;

    /**
     * 标题路径，例如：
     * 登录模块 > 手机号验证码登录 > 异常场景
     */
    private String sectionPath;

    /**
     * 所属模块
     */
    private String moduleName;

    /**
     * 需求编号，例如 REQ-001
     */
    private String requirementId;

    /**
     * 原文起始位置
     */
    private Integer startPosition;

    /**
     * 原文结束位置
     */
    private Integer endPosition;

    /**
     * 父 chunk，当前阶段先预留，后续 Parent-Child Chunk 使用
     */
    private Long parentChunkId;

    /**
     * metadata JSON 字符串
     */
    private String metadata;
}