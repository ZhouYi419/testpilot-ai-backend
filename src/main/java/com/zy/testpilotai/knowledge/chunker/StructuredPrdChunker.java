package com.zy.testpilotai.knowledge.chunker;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 结构化 PRD 切片器。
 */
@Primary
@Component
public class StructuredPrdChunker implements TextChunker {

    /**
     * 单个 chunk 最大字符数
     */
    private static final int MAX_CHUNK_SIZE = 1000;

    /**
     * 长文本切片重叠字符数
     */
    private static final int OVERLAP_SIZE = 120;

    /**
     * 小于这个长度时，尽量不切
     */
    private static final int MIN_CHUNK_SIZE = 300;

    private static final Pattern MARKDOWN_HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$");

    private static final Pattern NUMBER_HEADING_PATTERN = Pattern.compile("^(\\d+(\\.\\d+)*)([、.．\\s]+)(.+)$");

    private static final Pattern CHINESE_HEADING_PATTERN = Pattern.compile("^([一二三四五六七八九十]+)[、.．]\\s*(.+)$");

    private static final Pattern CHAPTER_HEADING_PATTERN = Pattern.compile("^第[一二三四五六七八九十\\d]+[章节部分篇].*$");

    private static final Pattern REQUIREMENT_ID_PATTERN = Pattern.compile("(REQ[-_A-Za-z0-9]+|[A-Z]{2,10}-\\d+|需求[编号ID]*[:：]?\\s*[A-Za-z0-9_-]+)");

    @Override
    public List<TextChunk> chunk(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }

        String normalizedText = normalize(text);
        List<DocumentBlock> blocks = parseBlocks(normalizedText);

        if (blocks.isEmpty()) {
            DocumentBlock block = new DocumentBlock(
                    "PARAGRAPH",
                    "全文",
                    "全文",
                    "全文",
                    null,
                    normalizedText,
                    0,
                    normalizedText.length()
            );
            blocks.add(block);
        }

        List<TextChunk> chunks = new ArrayList<>();
        int chunkIndex = 0;

        for (DocumentBlock block : blocks) {
            List<TextChunk> blockChunks = splitBlock(block, chunkIndex);
            chunks.addAll(blockChunks);
            chunkIndex += blockChunks.size();
        }

        return chunks;
    }

    /**
     * 把文本解析成结构块。
     */
    private List<DocumentBlock> parseBlocks(String text) {
        List<DocumentBlock> blocks = new ArrayList<>();

        String[] lines = text.split("\\n", -1);
        List<String> sectionStack = new ArrayList<>();

        StringBuilder currentContent = new StringBuilder();
        String currentTitle = "全文";
        String currentBlockType = "PARAGRAPH";
        String currentModuleName = "全文";
        String currentRequirementId = null;

        int cursor = 0;
        int blockStart = 0;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            int lineStart = cursor;
            cursor += rawLine.length() + 1;

            Heading heading = parseHeading(line);

            if (heading != null) {
                flushBlock(
                        blocks,
                        currentBlockType,
                        currentTitle,
                        buildSectionPath(sectionStack),
                        currentModuleName,
                        currentRequirementId,
                        currentContent,
                        blockStart,
                        lineStart
                );

                updateSectionStack(sectionStack, heading.level(), heading.title());

                currentTitle = heading.title();
                currentModuleName = detectModuleName(sectionStack);
                currentRequirementId = extractRequirementId(heading.title());
                currentBlockType = currentRequirementId == null ? "SECTION" : "REQUIREMENT";

                currentContent = new StringBuilder();
                currentContent.append(heading.title()).append("\n");
                blockStart = lineStart;
                continue;
            }

            if (StringUtils.hasText(line)) {
                currentContent.append(rawLine).append("\n");
            }
        }

        flushBlock(
                blocks,
                currentBlockType,
                currentTitle,
                buildSectionPath(sectionStack),
                currentModuleName,
                currentRequirementId,
                currentContent,
                blockStart,
                text.length()
        );

        return blocks;
    }

    private void flushBlock(
            List<DocumentBlock> blocks,
            String blockType,
            String title,
            String sectionPath,
            String moduleName,
            String requirementId,
            StringBuilder content,
            int startPosition,
            int endPosition
    ) {
        if (content == null || !StringUtils.hasText(content.toString())) {
            return;
        }

        String blockContent = content.toString().trim();
        String finalBlockType = detectBlockType(blockType, blockContent, requirementId);

        blocks.add(new DocumentBlock(
                finalBlockType,
                title,
                StringUtils.hasText(sectionPath) ? sectionPath : title,
                StringUtils.hasText(moduleName) ? moduleName : title,
                requirementId,
                blockContent,
                startPosition,
                endPosition
        ));
    }

    private List<TextChunk> splitBlock(DocumentBlock block, int startChunkIndex) {
        String content = block.getContent();

        if (!StringUtils.hasText(content)) {
            return List.of();
        }

        if (content.length() <= MAX_CHUNK_SIZE) {
            TextChunk chunk = buildTextChunk(block, startChunkIndex, content, block.getStartPosition(), block.getEndPosition());
            return List.of(chunk);
        }

        List<TextChunk> chunks = new ArrayList<>();

        int length = content.length();
        int start = 0;
        int chunkIndex = startChunkIndex;

        while (start < length) {
            int end = Math.min(start + MAX_CHUNK_SIZE, length);

            if (end < length) {
                int paragraphBreak = content.lastIndexOf("\n\n", end);
                if (paragraphBreak > start + MIN_CHUNK_SIZE) {
                    end = paragraphBreak;
                }
            }

            String chunkContent = content.substring(start, end).trim();

            if (StringUtils.hasText(chunkContent)) {
                int absoluteStart = block.getStartPosition() == null ? start : block.getStartPosition() + start;
                int absoluteEnd = block.getStartPosition() == null ? end : block.getStartPosition() + end;

                chunks.add(buildTextChunk(block, chunkIndex, chunkContent, absoluteStart, absoluteEnd));
                chunkIndex++;
            }

            if (end >= length) {
                break;
            }

            start = Math.max(0, end - OVERLAP_SIZE);
        }

        return chunks;
    }

    private TextChunk buildTextChunk(
            DocumentBlock block,
            int chunkIndex,
            String content,
            Integer startPosition,
            Integer endPosition
    ) {
        TextChunk chunk = new TextChunk();
        chunk.setChunkIndex(chunkIndex);
        chunk.setTitle(block.getTitle());
        chunk.setContent(content);
        chunk.setTokenCount(estimateTokenCount(content));
        chunk.setChunkType(block.getBlockType());
        chunk.setSectionPath(block.getSectionPath());
        chunk.setModuleName(block.getModuleName());
        chunk.setRequirementId(block.getRequirementId());
        chunk.setStartPosition(startPosition);
        chunk.setEndPosition(endPosition);
        chunk.setParentChunkId(null);
        chunk.setMetadata(buildMetadata(chunk));
        return chunk;
    }

    private Heading parseHeading(String line) {
        if (!StringUtils.hasText(line)) {
            return null;
        }

        Matcher markdownMatcher = MARKDOWN_HEADING_PATTERN.matcher(line);
        if (markdownMatcher.matches()) {
            int level = markdownMatcher.group(1).length();
            String title = markdownMatcher.group(2).trim();
            return new Heading(level, cleanHeadingTitle(title));
        }

        Matcher numberMatcher = NUMBER_HEADING_PATTERN.matcher(line);
        if (numberMatcher.matches() && line.length() <= 80) {
            String number = numberMatcher.group(1);
            String title = numberMatcher.group(4).trim();
            int level = Math.min(number.split("\\.").length + 1, 6);
            return new Heading(level, cleanHeadingTitle(title));
        }

        Matcher chineseMatcher = CHINESE_HEADING_PATTERN.matcher(line);
        if (chineseMatcher.matches() && line.length() <= 80) {
            String title = chineseMatcher.group(2).trim();
            return new Heading(2, cleanHeadingTitle(title));
        }

        if (CHAPTER_HEADING_PATTERN.matcher(line).matches() && line.length() <= 80) {
            return new Heading(1, cleanHeadingTitle(line));
        }

        if (looksLikeModuleHeading(line)) {
            return new Heading(2, cleanHeadingTitle(line));
        }

        return null;
    }

    private boolean looksLikeModuleHeading(String line) {
        if (!StringUtils.hasText(line)) {
            return false;
        }

        if (line.length() > 40) {
            return false;
        }

        return line.endsWith("模块")
                || line.endsWith("功能")
                || line.endsWith("页面")
                || line.endsWith("流程")
                || line.endsWith("规则")
                || line.endsWith("场景")
                || line.endsWith("说明");
    }

    private void updateSectionStack(List<String> sectionStack, int level, String title) {
        int targetSize = Math.max(level - 1, 0);

        while (sectionStack.size() > targetSize) {
            sectionStack.remove(sectionStack.size() - 1);
        }

        sectionStack.add(title);
    }

    private String buildSectionPath(List<String> sectionStack) {
        if (sectionStack == null || sectionStack.isEmpty()) {
            return "全文";
        }

        return String.join(" > ", sectionStack);
    }

    private String detectModuleName(List<String> sectionStack) {
        if (sectionStack == null || sectionStack.isEmpty()) {
            return "全文";
        }

        for (String section : sectionStack) {
            if (section.contains("模块")
                    || section.contains("功能")
                    || section.contains("页面")
                    || section.contains("流程")) {
                return section;
            }
        }

        return sectionStack.get(0);
    }

    private String extractRequirementId(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }

        Matcher matcher = REQUIREMENT_ID_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        return null;
    }

    private String detectBlockType(String defaultType, String content, String requirementId) {
        if (StringUtils.hasText(requirementId)) {
            return "REQUIREMENT";
        }

        if (looksLikeTable(content)) {
            return "TABLE";
        }

        if (StringUtils.hasText(defaultType)) {
            return defaultType;
        }

        return "PARAGRAPH";
    }

    private boolean looksLikeTable(String content) {
        if (!StringUtils.hasText(content)) {
            return false;
        }

        String[] lines = content.split("\\n");
        int tableLikeLineCount = 0;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.contains("|") && trimmed.split("\\|").length >= 3) {
                tableLikeLineCount++;
            }

            if (trimmed.contains("\t")) {
                tableLikeLineCount++;
            }
        }

        return tableLikeLineCount >= 2;
    }

    private String cleanHeadingTitle(String title) {
        if (title == null) {
            return "";
        }

        return title
                .replaceAll("^#+\\s*", "")
                .replaceAll("^\\d+(\\.\\d+)*[、.．\\s]+", "")
                .trim();
    }

    private String normalize(String text) {
        return text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    private Integer estimateTokenCount(String content) {
        if (content == null) {
            return 0;
        }

        return content.length();
    }

    private String buildMetadata(TextChunk chunk) {
        return """
                {
                  "chunkType": "%s",
                  "sectionPath": "%s",
                  "moduleName": "%s",
                  "requirementId": "%s",
                  "startPosition": %d,
                  "endPosition": %d,
                  "sourceType": "PRD"
                }
                """.formatted(
                escapeJson(chunk.getChunkType()),
                escapeJson(chunk.getSectionPath()),
                escapeJson(chunk.getModuleName()),
                escapeJson(chunk.getRequirementId()),
                chunk.getStartPosition() == null ? 0 : chunk.getStartPosition(),
                chunk.getEndPosition() == null ? 0 : chunk.getEndPosition()
        );
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private record Heading(int level, String title) {
    }
}