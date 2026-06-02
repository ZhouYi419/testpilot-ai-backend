package com.zy.testpilotai.knowledge.service.impl;

import com.zy.testpilotai.ai.embedding.EmbeddingClient;
import com.zy.testpilotai.ai.embedding.VectorUtils;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.document.mapper.PrdDocumentMapper;
import com.zy.testpilotai.document.model.entity.PrdDocumentEntity;
import com.zy.testpilotai.knowledge.mapper.KnowledgeChunkMapper;
import com.zy.testpilotai.knowledge.model.entity.KnowledgeChunkEntity;
import com.zy.testpilotai.knowledge.service.KnowledgeEmbeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeEmbeddingServiceImpl implements KnowledgeEmbeddingService {

    private final PrdDocumentMapper prdDocumentMapper;

    private final KnowledgeChunkMapper knowledgeChunkMapper;

    private final EmbeddingClient embeddingClient;

    @Override
    public Integer embedDocumentChunks(Long documentId) {
        if (documentId == null || documentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文档ID不合法");
        }

        PrdDocumentEntity document = prdDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "PRD 文档不存在");
        }

        if (!"CHUNKED".equals(document.getIndexStatus()) && !"PARTIAL_EMBEDDED".equals(document.getIndexStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "文档尚未完成切片，当前索引状态：" + document.getIndexStatus()
            );
        }

        List<KnowledgeChunkEntity> chunks = knowledgeChunkMapper.selectEnabledWithoutEmbeddingByDocumentId(documentId);
        if (chunks.isEmpty()) {
            prdDocumentMapper.updateIndexStatus(documentId, "EMBEDDED", null);
            return 0;
        }

        int successCount = 0;
        int failCount = 0;

        for (KnowledgeChunkEntity chunk : chunks) {
            try {
                if (!StringUtils.hasText(chunk.getContent())) {
                    knowledgeChunkMapper.updateEmbeddingStatus(chunk.getId(), "FAILED", "chunk 内容为空");
                    failCount++;
                    continue;
                }

                knowledgeChunkMapper.updateEmbeddingStatus(chunk.getId(), "EMBEDDING", null);

                List<Double> vector = embeddingClient.embed(chunk.getContent());
                String pgVector = VectorUtils.toPgVectorString(vector);

                knowledgeChunkMapper.updateEmbedding(
                        chunk.getId(),
                        pgVector,
                        embeddingClient.getModelName(),
                        "EMBEDDED",
                        null
                );

                successCount++;
            } catch (Exception e) {
                failCount++;
                knowledgeChunkMapper.updateEmbeddingStatus(chunk.getId(), "FAILED", e.getMessage());
            }
        }

        if (failCount > 0 && successCount > 0) {
            prdDocumentMapper.updateIndexStatus(documentId, "PARTIAL_EMBEDDED", "部分 chunk 向量化失败，失败数量：" + failCount);
        } else if (failCount > 0) {
            prdDocumentMapper.updateIndexStatus(documentId, "FAILED", "全部 chunk 向量化失败，失败数量：" + failCount);
        } else {
            prdDocumentMapper.updateIndexStatus(documentId, "EMBEDDED", null);
        }

        return successCount;
    }
}