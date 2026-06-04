package com.zy.testpilotai.document.chunk;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Component
public class ParentChildChunker {

    /**
     * 父级切片的最大字符数
     */
    private static final int MAX_PARENT_CHARS = 5000;

    /**
     * 子级切片的最大字符数
     */
    private static final int MAX_CHILD_CHARS = 1200;

    /**
     * 子级切片的重叠字符数 (Overlap)
     */
    private static final int CHILD_OVERLAP_CHARS = 200;

    /**
     * 执行全量文档切分
     */
    public List<ChunkGroup> split(String rawText) {
        // 1. 文本标准化（清理多余换行、空格）
        String normalizedText = normalize(rawText);

        // 2. 粗粒度切分：根据标题特征提取出父级切片
        List<ParentTextChunk> parents = splitParentChunks(normalizedText);

        List<ChunkGroup> groups = new ArrayList<>();
        int childGlobalIndex = 0; // 记录子块在整个文档中的全局索引

        // 3. 细粒度切分：遍历每个父块，生成包含重叠(Overlap)的子块
        for (ParentTextChunk parent : parents) {
            List<ChildTextChunk> children = splitChildChunks(
                    parent.getSectionTitle(),
                    parent.getContent(),
                    childGlobalIndex
            );

            childGlobalIndex += children.size();
            // 将父块与它拆分出来的子块封装进一个 Group 中返回
            groups.add(new ChunkGroup(parent, children));
        }

        return groups;
    }

    /**
     * 粗略估算文本对应的 Token 数量
     */
    public int estimateTokenCount(String text) {
        if (!StringUtils.hasText(text)) {
            return 0;
        }

        int chineseCharCount = 0;
        int asciiCharCount = 0;
        int otherCharCount = 0;

        for (char c : text.toCharArray()) {
            if (c >= '\u4e00' && c <= '\u9fff') {
                chineseCharCount++; // 汉字
            } else if (c < 128) {
                asciiCharCount++; // 纯英文及标点
            } else {
                otherCharCount++; // 其他字符
            }
        }

        // 估算经验值：
        // 英文单词平均约等于 4 个字符（4 chars ≈ 1 token）
        // 中文通常采用 BPE 编码，1个汉字约占 1.2 个 token
        // 其他字符粗略按 3个字符计算
        int englishTokenEstimate = asciiCharCount / 4;
        int chineseTokenEstimate = (int) Math.ceil(chineseCharCount * 1.2);
        int otherTokenEstimate = otherCharCount / 3;

        return englishTokenEstimate + chineseTokenEstimate + otherTokenEstimate;
    }

    /**
     * 文本标准化清理
     */
    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\r\n", "\n") // 统一 Windows 换行符
                .replace("\r", "\n")   // 统一 Mac 旧版换行符
                .replaceAll("[ \\t]+", " ") // 将连续的空格或制表符缩减为单个空格
                .replaceAll("\\n{3,}", "\n\n") // 将 3 个以上的连续换行合并为 2 个，保持段落分割
                .trim();
    }

    /**
     * 按章节标题将长文本切分为父级切片
     */
    private List<ParentTextChunk> splitParentChunks(String text) {
        List<ParentTextChunk> parents = new ArrayList<>();
        if (!StringUtils.hasText(text)) {
            return parents;
        }

        String[] lines = text.split("\n");
        String currentTitle = "文档开始"; // 默认初始标题
        StringBuilder currentContent = new StringBuilder();

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (!StringUtils.hasText(trimmedLine)) {
                currentContent.append("\n");
                continue;
            }

            // 命中标题特征，且当前已积累了上一章节的内容，则结算上一章节
            if (isHeading(trimmedLine) && !currentContent.isEmpty()) {
                addParentChunk(parents, currentTitle, currentContent.toString());
                // 更新为新章节的标题，并重新开始累积内容
                currentTitle = cleanHeading(trimmedLine);
                currentContent = new StringBuilder();
                currentContent.append(trimmedLine).append("\n");
            } else {
                // 如果命中标题但没内容（例如连续标题），只更新标题名
                if (isHeading(trimmedLine)) {
                    currentTitle = cleanHeading(trimmedLine);
                }
                currentContent.append(trimmedLine).append("\n");
            }
        }

        // 结算最后一段未归档的内容
        if (!currentContent.isEmpty()) {
            addParentChunk(parents, currentTitle, currentContent.toString());
        }

        return parents;
    }

    /**
     * 将解析出的大章节加入父切片集合，超长时触发降级拆分
     */
    private void addParentChunk(List<ParentTextChunk> parents, String title, String content) {
        String cleanContent = content == null ? "" : content.trim();
        if (!StringUtils.hasText(cleanContent)) {
            return;
        }

        // 如果内容在最大限制内，直接生成父块
        if (cleanContent.length() <= MAX_PARENT_CHARS) {
            parents.add(new ParentTextChunk(title, cleanContent, parents.size()));
            return;
        }

        // 如果单个章节内容过于庞大，超过了 MAX_PARENT_CHARS，按段落进一步强制拆分
        List<String> splitContents = splitLongTextByParagraph(cleanContent, MAX_PARENT_CHARS);

        for (int i = 0; i < splitContents.size(); i++) {
            // 给超长章节加上 (续 x) 的后缀标识
            String sectionTitle = i == 0 ? title : title + "（续 " + i + "）";
            parents.add(new ParentTextChunk(sectionTitle, splitContents.get(i), parents.size()));
        }
    }

    /**
     * 针对超长父切片的兜底切分策略（按空行/段落切分）
     */
    private List<String> splitLongTextByParagraph(String text, int maxChars) {
        List<String> result = new ArrayList<>();
        // 按双换行（段落分隔符）拆分
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder current = new StringBuilder();

        for (String paragraph : paragraphs) {
            // 如果加入新段落后超长，则把前面累积的先保存
            if (current.length() + paragraph.length() > maxChars && current.length() > 0) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            }
            current.append(paragraph).append("\n\n");
        }

        if (!current.isEmpty()) {
            result.add(current.toString().trim());
        }
        return result;
    }

    /**
     * 将父切片进一步拆分为带重叠区（Overlap）的子切片
     */
    private List<ChildTextChunk> splitChildChunks(String sectionTitle, String content, int startIndex) {
        List<ChildTextChunk> children = new ArrayList<>();
        if (!StringUtils.hasText(content)) {
            return children;
        }

        if (content.length() <= MAX_CHILD_CHARS) {
            children.add(new ChildTextChunk(sectionTitle, content.trim(), startIndex));
            return children;
        }

        int start = 0;
        int currentIndex = startIndex;

        // 使用滑动窗口算法进行切片
        while (start < content.length()) {
            int end = Math.min(start + MAX_CHILD_CHARS, content.length());

            // 智能调整结束位置，避免将一个句子生硬切断
            int adjustedEnd = adjustEndPosition(content, start, end);
            String childContent = content.substring(start, adjustedEnd).trim();

            if (StringUtils.hasText(childContent)) {
                children.add(new ChildTextChunk(sectionTitle, childContent, currentIndex));
                currentIndex++;
            }

            if (adjustedEnd >= content.length()) {
                break;
            }

            // 计算下一个切片的起始位置，预留出 CHILD_OVERLAP_CHARS 大小的重叠区域
            start = Math.max(0, adjustedEnd - CHILD_OVERLAP_CHARS);

            // 防止死循环的兜底：确保指针始终在前进
            if (start >= adjustedEnd) {
                start = adjustedEnd;
            }
        }

        return children;
    }

    /**
     * 智能边界回退机制：寻找合适的断句点（换行符或句号）
     */
    private int adjustEndPosition(String content, int start, int end) {
        if (end >= content.length()) {
            return end;
        }

        // 设定最小回退界限，防止为了找句号导致切片过小
        int minEnd = start + MAX_CHILD_CHARS / 2;

        // 优先尝试找前一个换行符作为切断点
        int lastNewLine = content.lastIndexOf("\n", end);
        if (lastNewLine > minEnd) {
            return lastNewLine;
        }

        // 如果找不到换行，尝试找中文或英文的句号
        int lastPeriod = Math.max(
                content.lastIndexOf("。", end),
                content.lastIndexOf(".", end)
        );

        if (lastPeriod > minEnd) {
            // 句号本身包含在这个切片里
            return lastPeriod + 1;
        }

        // 句号都没找到，进行硬切断
        return end;
    }

    /**
     * 通过正则表达式判断当前行是否为标题（如 Markdown的 '#' 或是中文序号 '一、'）
     */
    private boolean isHeading(String line) {
        if (!StringUtils.hasText(line)) {
            return false;
        }

        String trimmed = line.trim();

        // 命中 Markdown 标题，如 "# 标题"
        if (trimmed.matches("^#{1,6}\\s+.+")) {
            return true;
        }

        // 标题通常不会太长，超过100个字符大概率是正文中的枚举列项
        if (trimmed.length() > 100) {
            return false;
        }

        // 命中常见的中文或数字章节序号体系：
        // "第一章 xxx"
        // "1.1.2 xxx"
        // "二、 xxx"
        return trimmed.matches("^第[一二三四五六七八九十百千万0-9]+[章节篇部分].*")
                || trimmed.matches("^\\d+(\\.\\d+)*[、.\\s]+.+")
                || trimmed.matches("^[一二三四五六七八九十]+[、.\\s]+.+");
    }

    /**
     * 清理标题格式，剥离 Markdown 的 '#' 等特殊标记，保留纯文本
     */
    private String cleanHeading(String line) {
        if (line == null) {
            return "未命名章节";
        }
        return line.replaceAll("^#{1,6}\\s+", "").trim();
    }
}