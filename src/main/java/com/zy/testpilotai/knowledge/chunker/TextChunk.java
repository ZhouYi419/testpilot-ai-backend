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
}