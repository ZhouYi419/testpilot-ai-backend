package com.zy.testpilotai.knowledge.chunker;

import java.util.List;

/**
 * 文本切片器
 */
public interface TextChunker {

    List<TextChunk> chunk(String text);
}