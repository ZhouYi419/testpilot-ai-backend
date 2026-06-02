package com.zy.testpilotai.knowledge.service.impl;

import com.zy.testpilotai.ai.llm.LlmClient;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeChatRequest;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeChatResponseVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeReferenceVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeSearchResultVO;
import com.zy.testpilotai.knowledge.service.KnowledgeChatService;
import com.zy.testpilotai.knowledge.service.KnowledgeSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class KnowledgeChatServiceImpl implements KnowledgeChatService {

    private final KnowledgeSearchService knowledgeSearchService;

    private final LlmClient llmClient;

    @Override
    public KnowledgeChatResponseVO chat(KnowledgeChatRequest request) {
        validateRequest(request);

        Integer topK = request.getTopK() == null ? 5 : request.getTopK();
        if (topK < 1) {
            topK = 5;
        }
        if (topK > 10) {
            topK = 10;
        }

        KnowledgeSearchRequest searchRequest = new KnowledgeSearchRequest();
        searchRequest.setProjectId(request.getProjectId());
        searchRequest.setVersionName(request.getVersionName());
        searchRequest.setQuery(request.getQuestion());
        searchRequest.setTopK(topK);
        searchRequest.setMinSimilarity(null);

        List<KnowledgeSearchResultVO> searchResults = knowledgeSearchService.search(searchRequest);

        KnowledgeChatResponseVO response = new KnowledgeChatResponseVO();
        response.setModelName(llmClient.getModelName());
        response.setReferences(toReferences(searchResults));

        if (searchResults == null || searchResults.isEmpty()) {
            response.setAnswer("知识库中没有检索到与该问题相关的 PRD 内容，无法基于知识库回答。建议先确认 PRD 是否已上传、解析、切片并完成向量化。");
            return response;
        }

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(request.getQuestion(), searchResults);

        String answer = llmClient.chat(systemPrompt, userPrompt);
        response.setAnswer(answer);
        return response;
    }

    private void validateRequest(KnowledgeChatRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }

        if (request.getProjectId() == null || request.getProjectId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "项目ID不合法");
        }

        if (!StringUtils.hasText(request.getQuestion())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "问题不能为空");
        }

        if (request.getQuestion().trim().length() > 2000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "问题不能超过2000个字符");
        }
    }

    private String buildSystemPrompt() {
        return """
                你是一个资深测试专家和产品需求分析专家。
                你正在帮助测试工程师基于 PRD 知识库理解需求、分析测试点、识别风险。

                你必须严格遵守以下规则：
                1. 只能基于我提供的【PRD 知识片段】回答。
                2. 如果知识片段中没有依据，必须明确说明“知识库中没有找到相关依据”。
                3. 不要编造 PRD 中不存在的功能。
                4. 不要把常识当成 PRD 事实。
                5. 回答要面向测试工程师，尽量给出可验证的测试关注点。
                6. 如果信息不足，要列出需要产品或研发补充确认的问题。
                7. 回答必须使用中文。
                """;
    }

    private String buildUserPrompt(String question, List<KnowledgeSearchResultVO> searchResults) {
        String context = buildContext(searchResults);

        return """
                用户问题：
                %s

                【PRD 知识片段】：
                %s

                请按以下结构回答：

                1. 结论
                直接回答用户问题。

                2. PRD 依据
                说明你依据了哪些知识片段。

                3. 测试关注点
                从测试工程师角度列出需要重点验证的点。

                4. 风险和待确认问题
                如果 PRD 信息不足，请列出需要补充确认的问题。
                """.formatted(question, context);
    }

    private String buildContext(List<KnowledgeSearchResultVO> searchResults) {
        StringBuilder context = new StringBuilder();
        AtomicInteger index = new AtomicInteger(1);

        for (KnowledgeSearchResultVO result : searchResults) {
            int currentIndex = index.getAndIncrement();

            context.append("\n")
                    .append("【片段").append(currentIndex).append("】\n")
                    .append("chunkId: ").append(result.getChunkId()).append("\n")
                    .append("documentId: ").append(result.getDocumentId()).append("\n")
                    .append("versionName: ").append(result.getVersionName()).append("\n")
                    .append("title: ").append(result.getTitle()).append("\n")
                    .append("similarity: ").append(result.getSimilarity()).append("\n")
                    .append("content:\n")
                    .append(result.getContent())
                    .append("\n");
        }

        return context.toString();
    }

    private List<KnowledgeReferenceVO> toReferences(List<KnowledgeSearchResultVO> searchResults) {
        if (searchResults == null) {
            return List.of();
        }

        return searchResults.stream()
                .map(this::toReference)
                .toList();
    }

    private KnowledgeReferenceVO toReference(KnowledgeSearchResultVO result) {
        KnowledgeReferenceVO reference = new KnowledgeReferenceVO();
        reference.setChunkId(result.getChunkId());
        reference.setProjectId(result.getProjectId());
        reference.setDocumentId(result.getDocumentId());
        reference.setVersionName(result.getVersionName());
        reference.setChunkIndex(result.getChunkIndex());
        reference.setTitle(result.getTitle());
        reference.setContent(result.getContent());
        reference.setSimilarity(result.getSimilarity());
        return reference;
    }
}