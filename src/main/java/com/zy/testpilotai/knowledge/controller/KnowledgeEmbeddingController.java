package com.zy.testpilotai.knowledge.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.knowledge.service.KnowledgeEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class KnowledgeEmbeddingController {

    private final KnowledgeEmbeddingService knowledgeEmbeddingService;

    /**
     * 对指定 PRD 文档下的 chunk 生成 embedding
     */
    @PostMapping("/api/documents/{documentId}/embeddings")
    public BaseResponse<Integer> embedDocumentChunks(@PathVariable Long documentId) {
        return ResultUtils.success(
                knowledgeEmbeddingService.embedDocumentChunks(documentId)
        );
    }
}