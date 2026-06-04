package com.zy.testpilotai.document.chunk;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParentTextChunk {

    /**
     * 章节/段落标题
     */
    private String sectionTitle;

    /**
     * 文本内容
     */
    private String content;

    /**
     * 切片排序索引
     */
    private Integer chunkIndex;
}