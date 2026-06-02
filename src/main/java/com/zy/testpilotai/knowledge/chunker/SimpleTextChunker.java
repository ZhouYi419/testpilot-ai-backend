package com.zy.testpilotai.knowledge.chunker;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * 1. 每个 chunk 约 1000 个字符
 * 2. chunk 之间保留 150 个字符重叠
 * 3. 优先在段落边界切开
 */
@Component
public class SimpleTextChunker implements TextChunker {

    private static final int MAX_CHUNK_SIZE = 1000;

    private static final int OVERLAP_SIZE = 150;

    private static final int MIN_CHUNK_SIZE = 300;

    @Override
    public List<TextChunk> chunk(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        String normalizedText = normalize(text);
        List<TextChunk> chunks = new ArrayList<>();

        int length = normalizedText.length();
        int start = 0;
        int chunkIndex = 0;

        while (start < length) {
            int end = Math.min(start + MAX_CHUNK_SIZE, length);

            if (end < length) {
                int paragraphBreak = normalizedText.lastIndexOf("\n\n", end);
                if (paragraphBreak > start + MIN_CHUNK_SIZE) {
                    end = paragraphBreak;
                }
            }

            String content = normalizedText.substring(start, end).trim();

            if (StringUtils.hasText(content)) {
                chunks.add(new TextChunk(
                        chunkIndex,
                        extractTitle(content),
                        content,
                        estimateTokenCount(content)
                ));
                chunkIndex++;
            }

            if (end >= length) {
                break;
            }

            start = Math.max(0, end - OVERLAP_SIZE);
        }

        return chunks;
    }

    private String normalize(String text) {
        return text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    /**
     * 1. 如果当前 chunk 第一行是 markdown 标题，直接用它
     * 2. 否则取第一行前 80 个字符
     */
    private String extractTitle(String content) {
        String[] lines = content.split("\n");
        if (lines.length == 0) {
            return null;
        }

        String firstLine = lines[0].trim();
        if (!StringUtils.hasText(firstLine)) {
            return null;
        }

        firstLine = firstLine.replaceAll("^#+\\s*", "").trim();

        if (firstLine.length() > 80) {
            return firstLine.substring(0, 80);
        }

        return firstLine;
    }

    private Integer estimateTokenCount(String content) {
        if (content == null) {
            return 0;
        }
        return content.length();
    }
}