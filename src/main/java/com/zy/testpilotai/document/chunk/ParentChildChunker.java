package com.zy.testpilotai.document.chunk;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.utils.JsonExtractUtils;
import com.zy.testpilotai.llm.chat.LlmClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Component
public class ParentChildChunker {

    // 父级切片最大字符数
    private static final int MAX_PARENT_CHARS = 5000;

    // 子级切片最大字符数
    private static final int MAX_CHILD_CHARS = 1200;

    // 子级切片滑动窗口重叠字符数
    private static final int CHILD_OVERLAP_CHARS = 200;

    // 喂给 LLM 分析切片策略时，最多截取的头部样本字符数（省 Token 且通常头部已包含文档特征）
    private static final int STRATEGY_SAMPLE_CHARS = 6000;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private LlmClient llmClient;

    @Autowired(required = false)
    public void setLlmClient(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    /**
     * 执行全量文档的智能切分
     */
    public List<ChunkGroup> split(String rawText) {
        // 1. 文本初步清洗
        String normalizedText = normalize(rawText);

        // 2. 动态决断切片策略
        ChunkStrategy strategy = resolveStrategy(normalizedText);

        // 3. 执行粗粒度父级切分
        List<ParentTextChunk> parents = splitParentChunks(normalizedText, strategy);

        List<ChunkGroup> groups = new ArrayList<>();
        int childGlobalIndex = 0; // 全局子块索引

        // 4. 遍历父块，执行细粒度子级切分
        for (ParentTextChunk parent : parents) {
            List<ChildTextChunk> children = splitChildChunks(
                    parent.getSectionTitle(),
                    parent.getContent(),
                    childGlobalIndex,
                    strategy
            );

            childGlobalIndex += children.size();
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
                chineseCharCount++;
            } else if (c < 128) {
                asciiCharCount++;
            } else {
                otherCharCount++;
            }
        }

        int englishTokenEstimate = asciiCharCount / 4;
        int chineseTokenEstimate = (int) Math.ceil(chineseCharCount * 1.2);
        int otherTokenEstimate = otherCharCount / 3;

        return englishTokenEstimate + chineseTokenEstimate + otherTokenEstimate;
    }

    /**
     * 文本标准化：清洗无用的空白字符，保留正常的段落格式
     */
    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }

    /**
     * 策略决断机制：优先使用大模型分析文档类型，若失败或未配置 LLM，则回退到本地启发式正则探测
     */
    private ChunkStrategy resolveStrategy(String text) {
        // 先生成兜底策略
        ChunkStrategy heuristicStrategy = buildHeuristicStrategy(text);
        if (llmClient == null || !StringUtils.hasText(text)) {
            return heuristicStrategy;
        }

        try {
            // 让 LLM 仅基于文档头部样本，输出结构化的策略 JSON
            String rawOutput = llmClient.chat(
                    buildStrategySystemPrompt(),
                    buildStrategyUserPrompt(text),
                    "DOCUMENT_CHUNK_STRATEGY",
                    "preview"
            );
            return parseStrategy(rawOutput, heuristicStrategy);
        } catch (Exception ignored) {
            // LLM 解析异常时，静默回退到本地正则策略
            return heuristicStrategy;
        }
    }

    private String buildStrategySystemPrompt() {
        return """
                你是文档切块策略分析器。你的任务不是切文本，而是选择切块策略。
                只能输出一个 JSON 对象，不要输出解释、Markdown 或多余文字。
                JSON 字段：
                {
                  "documentType": "generic|prd|api|protocol|meeting|unknown",
                  "parentRules": ["markdown_heading_1_to_3", "coarse_number_heading", "chapter_heading"],
                  "childRules": ["markdown_subheading", "message_id_block", "enum_heading", "json_block", "paragraph", "sentence", "length_window"],
                  "maxParentChars": 5000,
                  "maxChildChars": 1200,
                  "overlapChars": 200,
                  "preserveBlocks": ["json", "code", "table"]
                }
                选择原则：结构优先，其次语义边界，最后长度窗口兜底。不要返回 chunk 内容。
                """;
    }

    private String buildStrategyUserPrompt(String text) {
        // 为省 Token 及提升响应速度，仅截取文档前部分作为样本供模型判断
        String sample = text.length() <= STRATEGY_SAMPLE_CHARS
                ? text
                : text.substring(0, STRATEGY_SAMPLE_CHARS);

        return """
                【切块策略输出 JSON 格式】
                请分析下面文档样本，返回适合它的切块策略 JSON。

                【文档样本】
                %s
                """.formatted(sample);
    }

    /**
     * 解析大模型返回的策略JSON
     */
    private ChunkStrategy parseStrategy(String rawOutput, ChunkStrategy fallback) {
        if (!StringUtils.hasText(rawOutput)) {
            return fallback;
        }

        try {
            String json = JsonExtractUtils.extractJsonObject(rawOutput);
            JsonNode root = objectMapper.readTree(json);

            ChunkStrategy strategy = fallback.copy();
            strategy.documentType = text(root, "documentType", strategy.documentType);
            strategy.parentRules = readStringSet(root.path("parentRules"), strategy.parentRules);
            strategy.childRules = readStringSet(root.path("childRules"), strategy.childRules);
            strategy.preserveBlocks = readStringSet(root.path("preserveBlocks"), strategy.preserveBlocks);

            // 防御性编程：限定 LLM 给出的长度范围，防止撑爆内存或 Token
            strategy.maxParentChars = boundedInt(root.path("maxParentChars").asInt(strategy.maxParentChars), 1000, 12_000);
            strategy.maxChildChars = boundedInt(root.path("maxChildChars").asInt(strategy.maxChildChars), 300, 3000);
            strategy.overlapChars = boundedInt(root.path("overlapChars").asInt(strategy.overlapChars), 0, strategy.maxChildChars / 2);

            strategy.ensureFallbackRules();
            return strategy;
        } catch (Exception ignored) {
            return fallback;
        }
    }

    /**
     * 基于正则和关键词匹配的本地启发式策略
     */
    private ChunkStrategy buildHeuristicStrategy(String text) {
        ChunkStrategy strategy = ChunkStrategy.generic();
        if (!StringUtils.hasText(text)) {
            return strategy;
        }

        if (looksLikeProtocolDocument(text)) {
            strategy.documentType = "protocol";
            strategy.childRules.add("message_id_block");
            strategy.childRules.add("enum_heading");
            strategy.childRules.add("json_block");
        } else if (looksLikeApiDocument(text)) {
            strategy.documentType = "api";
            strategy.childRules.add("json_block");
            strategy.preserveBlocks.add("json");
        } else if (looksLikePrdDocument(text)) {
            strategy.documentType = "prd";
        }

        strategy.ensureFallbackRules();
        return strategy;
    }

    // --- 文档类型特征探测方法 ---

    private boolean looksLikeProtocolDocument(String text) {
        return text.contains("\"msgId\"")
                || text.contains("msgId")
                || text.matches("(?s).*\\n\\s*\\d{4,6}\\s*:?\\s*\\n.*")
                || text.contains("transcribeType")
                || text.contains("TransStatus");
    }

    private boolean looksLikeApiDocument(String text) {
        return text.contains("请求参数")
                || text.contains("响应参数")
                || text.contains("接口地址")
                || text.contains("HTTPHeaderField")
                || text.contains("HTTP Header");
    }

    private boolean looksLikePrdDocument(String text) {
        return text.contains("需求背景")
                || text.contains("功能说明")
                || text.contains("验收标准")
                || text.contains("业务规则");
    }

    /**
     * 执行父级大块切分
     */
    private List<ParentTextChunk> splitParentChunks(String text, ChunkStrategy strategy) {
        List<ParentTextChunk> parents = new ArrayList<>();
        if (!StringUtils.hasText(text)) {
            return parents;
        }

        // 探测文档是否包含规范的 Markdown 标题结构（如 # 标题）
        boolean preferMarkdownHeading = hasCoarseMarkdownHeading(text);
        String[] lines = text.split("\n");
        String currentTitle = "文档开始";
        StringBuilder currentContent = new StringBuilder();

        for (String line : lines) {
            String trimmedLine = line.trim();

            if (!StringUtils.hasText(trimmedLine)) {
                currentContent.append("\n");
                continue;
            }

            boolean heading = isParentHeading(trimmedLine, preferMarkdownHeading);

            if (heading && !currentContent.isEmpty()) {
                addParentChunk(parents, currentTitle, currentContent.toString(), strategy);
                currentTitle = cleanHeading(trimmedLine);
                currentContent = new StringBuilder();
                currentContent.append(trimmedLine).append("\n");
            } else {
                if (heading) {
                    currentTitle = cleanHeading(trimmedLine);
                }
                currentContent.append(trimmedLine).append("\n");
            }
        }

        if (!currentContent.isEmpty()) {
            addParentChunk(parents, currentTitle, currentContent.toString(), strategy);
        }

        // 合并过短的父章节，避免后续检索时上下文过于碎片化
        return mergeShortParentChunks(parents, strategy);
    }

    /**
     * 将解析出的章节加入父块，处理超长章节拆分
     */
    private void addParentChunk(
            List<ParentTextChunk> parents,
            String title,
            String content,
            ChunkStrategy strategy
    ) {
        String cleanContent = content == null ? "" : content.trim();
        if (!StringUtils.hasText(cleanContent)) {
            return;
        }

        if (cleanContent.length() <= strategy.maxParentChars) {
            parents.add(new ParentTextChunk(title, cleanContent, parents.size()));
            return;
        }

        // 章节内容超出最大限制，按段落进一步降级拆分
        List<String> splitContents = splitLongTextByParagraph(cleanContent, strategy.maxParentChars);

        for (int i = 0; i < splitContents.size(); i++) {
            String sectionTitle = i == 0 ? title : title + "（续 " + i + "）";
            parents.add(new ParentTextChunk(sectionTitle, splitContents.get(i), parents.size()));
        }
    }

    /**
     * 按段落拆分长文本（兜底策略）
     */
    private List<String> splitLongTextByParagraph(String text, int maxChars) {
        List<String> result = new ArrayList<>();
        String[] paragraphs = text.split("\\n\\s*\\n");
        StringBuilder current = new StringBuilder();

        for (String paragraph : paragraphs) {
            String cleanParagraph = paragraph.trim();
            if (!StringUtils.hasText(cleanParagraph)) {
                continue;
            }

            // 如果单段已经超过最大限制，走固定长度强拆
            if (cleanParagraph.length() > maxChars) {
                if (!current.isEmpty()) {
                    result.add(current.toString().trim());
                    current = new StringBuilder();
                }
                result.addAll(splitByFixedLength(cleanParagraph, maxChars));
                continue;
            }

            if (current.length() + cleanParagraph.length() > maxChars && current.length() > 0) {
                result.add(current.toString().trim());
                current = new StringBuilder();
            }
            current.append(cleanParagraph).append("\n\n");
        }

        if (!current.isEmpty()) {
            result.add(current.toString().trim());
        }
        return result;
    }

    /**
     * 固定长度拆分
     */
    private List<String> splitByFixedLength(String text, int maxChars) {
        List<String> result = new ArrayList<>();
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + maxChars, text.length());
            result.add(text.substring(start, end).trim());
            start = end;
        }
        return result;
    }

    /**
     * 将内容过短的连续父章节合并为单个上下文
     * 例如："1.1 背景" (很短) + "1.2 目的" (很短) -> 合并为一个大 Chunk
     */
    private List<ParentTextChunk> mergeShortParentChunks(List<ParentTextChunk> candidates, ChunkStrategy strategy) {
        List<ParentTextChunk> merged = new ArrayList<>();
        ParentTextChunk pending = null;

        for (ParentTextChunk candidate : candidates) {
            if (pending == null) {
                pending = candidate;
                continue;
            }

            if (pending.getContent().length() < strategy.maxChildChars && canMergeParent(pending, candidate, strategy)) {
                pending = mergeParent(pending, candidate);

                if (pending.getContent().length() >= strategy.maxChildChars) {
                    addReindexedParent(merged, pending);
                    pending = null;
                }
                continue;
            }

            addReindexedParent(merged, pending);
            pending = candidate;
        }

        if (pending != null) {
            if (pending.getContent().length() < strategy.maxChildChars && !merged.isEmpty()) {
                ParentTextChunk last = merged.get(merged.size() - 1);
                if (canMergeParent(last, pending, strategy)) {
                    merged.set(merged.size() - 1, reindexParent(mergeParent(last, pending), merged.size() - 1));
                    return merged;
                }
            }
            addReindexedParent(merged, pending);
        }

        return merged;
    }

    private boolean canMergeParent(ParentTextChunk current, ParentTextChunk next, ChunkStrategy strategy) {
        return current.getContent().length() + next.getContent().length() + 2 <= strategy.maxParentChars;
    }

    private ParentTextChunk mergeParent(ParentTextChunk current, ParentTextChunk next) {
        String mergedTitle = mergeTitle(current.getSectionTitle(), next.getSectionTitle());
        String mergedContent = current.getContent().trim() + "\n\n" + next.getContent().trim();
        return new ParentTextChunk(mergedTitle, mergedContent, current.getChunkIndex());
    }

    private String mergeTitle(String currentTitle, String nextTitle) {
        if (!StringUtils.hasText(currentTitle)) {
            return nextTitle;
        }
        if (!StringUtils.hasText(nextTitle) || currentTitle.equals(nextTitle)) {
            return currentTitle;
        }
        return currentTitle + " / " + nextTitle;
    }

    private void addReindexedParent(List<ParentTextChunk> parents, ParentTextChunk parent) {
        parents.add(reindexParent(parent, parents.size()));
    }

    private ParentTextChunk reindexParent(ParentTextChunk parent, int chunkIndex) {
        return new ParentTextChunk(parent.getSectionTitle(), parent.getContent(), chunkIndex);
    }

    /**
     * 将父切片进一步拆分为用于向量化（Embedding）的子切片
     */
    private List<ChildTextChunk> splitChildChunks(
            String sectionTitle,
            String content,
            int startIndex,
            ChunkStrategy strategy
    ) {
        List<ChildTextChunk> children = new ArrayList<>();
        if (!StringUtils.hasText(content)) {
            return children;
        }

        // 尝试基于换行、标题、代码块等规则，先解析出独立的语义单元
        List<String> semanticUnits = splitSemanticUnits(content, strategy);

        if (semanticUnits.size() > 1) {
            // 对于像通信协议这种具有严格强边界（如 Message ID 分割）的文档
            if (strategy.hasHardChildBoundary()) {
                return createChildrenFromHardUnits(sectionTitle, semanticUnits, startIndex, strategy);
            }
            // 普通文本，尽量将多个小单元打包进一个符合长度的 Child Chunk 中
            return packSemanticUnits(sectionTitle, semanticUnits, startIndex, strategy);
        }

        // 如果内容本来就在限制内，无需切分
        if (content.length() <= strategy.maxChildChars) {
            children.add(new ChildTextChunk(sectionTitle, content.trim(), startIndex));
            return children;
        }

        // 如果无法提取出清晰的语义边界单元，只能走最基础的滑动窗口硬切
        int currentIndex = startIndex;
        for (String childContent : splitByWindow(content, strategy.maxChildChars, strategy.overlapChars)) {
            if (StringUtils.hasText(childContent)) {
                children.add(new ChildTextChunk(sectionTitle, childContent, currentIndex));
                currentIndex++;
            }
        }

        return children;
    }

    /**
     * 强边界单元切分处理
     */
    private List<ChildTextChunk> createChildrenFromHardUnits(
            String sectionTitle,
            List<String> units,
            int startIndex,
            ChunkStrategy strategy
    ) {
        List<ChildTextChunk> children = new ArrayList<>();
        String contextPrefix = "";
        int currentIndex = startIndex;

        for (String unit : units) {
            String cleanUnit = unit.trim();
            if (!StringUtils.hasText(cleanUnit)) {
                continue;
            }

            // 若提取到的是独立的标题，将其作为上下文前缀挂靠到接下来的内容上
            if (isStandaloneHeadingUnit(cleanUnit)) {
                contextPrefix = cleanUnit;
                continue;
            }

            String childContent = StringUtils.hasText(contextPrefix)
                    ? contextPrefix + "\n\n" + cleanUnit
                    : cleanUnit;

            if (childContent.length() > strategy.maxChildChars) {
                for (String window : splitByWindow(childContent, strategy.maxChildChars, strategy.overlapChars)) {
                    children.add(new ChildTextChunk(sectionTitle, window, currentIndex));
                    currentIndex++;
                }
                continue;
            }

            children.add(new ChildTextChunk(sectionTitle, childContent, currentIndex));
            currentIndex++;
        }

        if (children.isEmpty() && StringUtils.hasText(contextPrefix)) {
            children.add(new ChildTextChunk(sectionTitle, contextPrefix, currentIndex));
        }

        return children;
    }

    private boolean isStandaloneHeadingUnit(String text) {
        String[] lines = text.split("\n");
        if (lines.length > 2) {
            return false;
        }

        for (String line : lines) {
            String trimmed = line.trim();
            if (StringUtils.hasText(trimmed) && !trimmed.matches("^#{1,6}\\s+.+")) {
                return false;
            }
        }

        return true;
    }

    /**
     * 语义单元打包装箱：将多个短句子/小段落合并到一个 Child 中，直到达到最大长度限制
     */
    private List<ChildTextChunk> packSemanticUnits(
            String sectionTitle,
            List<String> units,
            int startIndex,
            ChunkStrategy strategy
    ) {
        List<ChildTextChunk> children = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int currentIndex = startIndex;

        for (String unit : units) {
            String cleanUnit = unit.trim();
            if (!StringUtils.hasText(cleanUnit)) {
                continue;
            }

            if (cleanUnit.length() > strategy.maxChildChars) {
                if (!current.isEmpty()) {
                    children.add(new ChildTextChunk(sectionTitle, current.toString().trim(), currentIndex));
                    currentIndex++;
                    current = new StringBuilder();
                }

                for (String window : splitByWindow(cleanUnit, strategy.maxChildChars, strategy.overlapChars)) {
                    children.add(new ChildTextChunk(sectionTitle, window, currentIndex));
                    currentIndex++;
                }
                continue;
            }

            int candidateLength = current.isEmpty()
                    ? cleanUnit.length()
                    : current.length() + cleanUnit.length() + 2;
            if (candidateLength > strategy.maxChildChars && !current.isEmpty()) {
                children.add(new ChildTextChunk(sectionTitle, current.toString().trim(), currentIndex));
                currentIndex++;
                current = new StringBuilder();
            }

            if (!current.isEmpty()) {
                current.append("\n\n");
            }
            current.append(cleanUnit);
        }

        if (!current.isEmpty()) {
            children.add(new ChildTextChunk(sectionTitle, current.toString().trim(), currentIndex));
        }

        return children;
    }

    /**
     * 核心语义解析：尝试安全地提取段落，同时避免破坏特定的结构
     */
    private List<String> splitSemanticUnits(String content, ChunkStrategy strategy) {
        List<String> units = new ArrayList<>();
        String[] lines = content.split("\n");
        StringBuilder current = new StringBuilder();
        boolean inCodeBlock = false;

        for (String line : lines) {
            String trimmed = line.trim();

            // 识别并保护 Markdown 代码块不被从内部拆断
            if (trimmed.startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                current.append(line).append("\n");
                continue;
            }

            // 命中策略定义的子级切分边界（且不在代码块内）
            boolean boundary = !inCodeBlock
                    && StringUtils.hasText(trimmed)
                    && !current.isEmpty()
                    && isChildBoundary(trimmed, strategy);

            if (boundary) {
                units.add(current.toString().trim());
                current = new StringBuilder();
            }

            current.append(line).append("\n");
        }

        if (!current.isEmpty()) {
            units.add(current.toString().trim());
        }

        // 如果按策略边界没拆出来，且配置允许按段落切分，则降级按段落切分
        if (units.size() <= 1 && strategy.hasChildRule("paragraph")) {
            return splitParagraphUnits(content);
        }

        return units;
    }

    /**
     * 按段落（双换行）切分
     */
    private List<String> splitParagraphUnits(String content) {
        List<String> units = new ArrayList<>();
        String[] paragraphs = content.split("\\n\\s*\\n");

        for (String paragraph : paragraphs) {
            String cleanParagraph = paragraph.trim();
            if (StringUtils.hasText(cleanParagraph)) {
                units.add(cleanParagraph);
            }
        }

        // 如果还是切不出来（例如全篇没有空行），继续降级按标点符号句子切分
        if (units.size() <= 1) {
            return splitSentenceUnits(content);
        }

        return units;
    }

    /**
     * 按断句标点符号（句号、感叹号、问号）切分
     */
    private List<String> splitSentenceUnits(String content) {
        List<String> units = new ArrayList<>();
        // 匹配中文或英文的句尾标点，且保留标点本身
        String[] sentences = content.split("(?<=[。！？.!?])\\s+|(?<=[。！？.!?])");

        StringBuilder current = new StringBuilder();
        for (String sentence : sentences) {
            String cleanSentence = sentence.trim();
            if (!StringUtils.hasText(cleanSentence)) {
                continue;
            }

            if (current.length() + cleanSentence.length() > MAX_CHILD_CHARS && !current.isEmpty()) {
                units.add(current.toString().trim());
                current = new StringBuilder();
            }

            current.append(cleanSentence);
        }

        if (!current.isEmpty()) {
            units.add(current.toString().trim());
        }

        return units;
    }

    /**
     * 判断当前行是否触发了特定的子切片边界规则
     */
    private boolean isChildBoundary(String line, ChunkStrategy strategy) {
        if (strategy.hasChildRule("markdown_subheading") && line.matches("^#{4,6}\\s+.+")) {
            return true;
        }

        // 针对协议文档：命中 MsgID 定义块
        if (strategy.hasChildRule("message_id_block") && line.matches("^\\d{4,6}\\s*:?\\s*$")) {
            return true;
        }

        // 针对 API 或结构体文档：命中枚举块定义
        if (strategy.hasChildRule("enum_heading")
                && line.matches("^#{4,6}\\s+.+")
                && containsAnyIgnoreCase(line, "enum", "type", "status", "枚举")) {
            return true;
        }

        return false;
    }

    private boolean containsAnyIgnoreCase(String text, String... keywords) {
        String lower = text.toLowerCase(Locale.ROOT);
        for (String keyword : keywords) {
            if (lower.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 基础滑动窗口切分算法
     */
    private List<String> splitByWindow(String content, int maxChars, int overlapChars) {
        List<String> result = new ArrayList<>();
        int start = 0;

        while (start < content.length()) {
            int end = Math.min(start + maxChars, content.length());
            // 智能回退：避免生硬截断长句子
            int adjustedEnd = adjustEndPosition(content, start, end, maxChars);
            String childContent = content.substring(start, adjustedEnd).trim();

            if (StringUtils.hasText(childContent)) {
                result.add(childContent);
            }

            if (adjustedEnd >= content.length()) {
                break;
            }

            // 计算下一个切片的起点（制造 Overlap 区间），并防死循环处理
            int nextStart = Math.max(0, adjustedEnd - overlapChars);
            start = nextStart <= start ? adjustedEnd : nextStart;
        }

        return result;
    }

    /**
     * 智能边界回退机制：寻找合适的断句点（换行符或句号），避免把一句话切成两截
     */
    private int adjustEndPosition(String content, int start, int end, int maxChars) {
        if (end >= content.length()) {
            return end;
        }

        int minEnd = start + maxChars / 2;

        int lastNewLine = content.lastIndexOf("\n", end);
        if (lastNewLine > minEnd) {
            return lastNewLine;
        }

        int lastPeriod = Math.max(
                content.lastIndexOf("。", end),
                content.lastIndexOf(".", end)
        );

        if (lastPeriod > minEnd) {
            return lastPeriod + 1;
        }

        return end;
    }

    // --- 以下为通用的标题匹配和策略配置类 ---

    private boolean isParentHeading(String line, boolean preferMarkdownHeading) {
        if (!StringUtils.hasText(line)) {
            return false;
        }

        String trimmed = line.trim();

        if (isCoarseMarkdownHeading(trimmed)) {
            return true;
        }

        if (preferMarkdownHeading) {
            return false;
        }

        if (trimmed.length() > 100) {
            return false;
        }

        return trimmed.matches("^第[一二三四五六七八九十百千万0-9]+[章节篇部分].*")
                || trimmed.matches("^\\d+[、.\\s]+.+")
                || trimmed.matches("^[一二三四五六七八九十]+[、.\\s]+.+");
    }

    private boolean hasCoarseMarkdownHeading(String text) {
        for (String line : text.split("\n")) {
            if (isCoarseMarkdownHeading(line.trim())) {
                return true;
            }
        }
        return false;
    }

    private boolean isCoarseMarkdownHeading(String line) {
        return StringUtils.hasText(line) && line.matches("^#{1,3}\\s+.+");
    }

    private String cleanHeading(String line) {
        if (line == null) {
            return "未命名章节";
        }
        return line.replaceAll("^#{1,6}\\s+", "").trim();
    }

    private String text(JsonNode node, String fieldName, String fallback) {
        String value = node.path(fieldName).asText(null);
        return StringUtils.hasText(value) ? value : fallback;
    }

    private Set<String> readStringSet(JsonNode node, Set<String> fallback) {
        if (!node.isArray()) {
            return new LinkedHashSet<>(fallback);
        }

        Set<String> result = new LinkedHashSet<>();
        for (JsonNode item : node) {
            String value = item.asText();
            if (StringUtils.hasText(value)) {
                result.add(value);
            }
        }

        return result.isEmpty() ? new LinkedHashSet<>(fallback) : result;
    }

    private int boundedInt(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    /**
     * 文档切片策略模型
     */
    private static class ChunkStrategy {

        // 文档类型
        private String documentType;
        // 允许作为父级边界的规则集合
        private Set<String> parentRules;
        // 允许作为子级边界的规则集合
        private Set<String> childRules;
        // 父级最大长度
        private int maxParentChars;
        // 子级最大长度
        private int maxChildChars;
        // 子级重叠区长度
        private int overlapChars;
        // 绝对不能拆断的语法块（如 JSON, Code）
        private Set<String> preserveBlocks;

        /**
         * 缺省的通用（Generic）兜底策略
         */
        private static ChunkStrategy generic() {
            ChunkStrategy strategy = new ChunkStrategy();
            strategy.documentType = "generic";
            strategy.parentRules = new LinkedHashSet<>(List.of(
                    "markdown_heading_1_to_3",
                    "chapter_heading",
                    "coarse_number_heading"
            ));
            strategy.childRules = new LinkedHashSet<>(List.of(
                    "markdown_subheading",
                    "paragraph",
                    "sentence",
                    "length_window"
            ));
            strategy.maxParentChars = MAX_PARENT_CHARS;
            strategy.maxChildChars = MAX_CHILD_CHARS;
            strategy.overlapChars = CHILD_OVERLAP_CHARS;
            strategy.preserveBlocks = new LinkedHashSet<>(List.of("json", "code", "table"));
            return strategy;
        }

        private ChunkStrategy copy() {
            ChunkStrategy strategy = new ChunkStrategy();
            strategy.documentType = documentType;
            strategy.parentRules = new LinkedHashSet<>(parentRules);
            strategy.childRules = new LinkedHashSet<>(childRules);
            strategy.maxParentChars = maxParentChars;
            strategy.maxChildChars = maxChildChars;
            strategy.overlapChars = overlapChars;
            strategy.preserveBlocks = new LinkedHashSet<>(preserveBlocks);
            return strategy;
        }

        private boolean hasChildRule(String rule) {
            return childRules.stream().anyMatch(item -> item.equalsIgnoreCase(rule));
        }

        private boolean hasHardChildBoundary() {
            return hasChildRule("message_id_block") || hasChildRule("enum_heading");
        }

        /**
         * 确保策略数据具有安全的默认值，防止空指针或错误分块
         */
        private void ensureFallbackRules() {
            if (parentRules == null || parentRules.isEmpty()) {
                parentRules = new LinkedHashSet<>(List.of("markdown_heading_1_to_3"));
            }
            if (childRules == null || childRules.isEmpty()) {
                childRules = new LinkedHashSet<>(List.of("paragraph", "sentence", "length_window"));
            }

            childRules.add("length_window");
            maxParentChars = maxParentChars <= 0 ? MAX_PARENT_CHARS : maxParentChars;
            maxChildChars = maxChildChars <= 0 ? MAX_CHILD_CHARS : maxChildChars;
            overlapChars = Math.max(0, Math.min(overlapChars, maxChildChars / 2));

            if (preserveBlocks == null) {
                preserveBlocks = new LinkedHashSet<>();
            }
        }
    }
}