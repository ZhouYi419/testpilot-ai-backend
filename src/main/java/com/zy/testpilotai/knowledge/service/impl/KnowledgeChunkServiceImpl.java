package com.zy.testpilotai.knowledge.service.impl;

import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.document.mapper.PrdDocumentMapper;
import com.zy.testpilotai.document.model.entity.PrdDocumentEntity;
import com.zy.testpilotai.knowledge.chunker.TextChunk;
import com.zy.testpilotai.knowledge.chunker.TextChunker;
import com.zy.testpilotai.knowledge.mapper.KnowledgeChunkMapper;
import com.zy.testpilotai.knowledge.model.entity.KnowledgeChunkEntity;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeChunkVO;
import com.zy.testpilotai.knowledge.service.KnowledgeChunkService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeChunkServiceImpl implements KnowledgeChunkService {

    private final PrdDocumentMapper prdDocumentMapper;

    private final KnowledgeChunkMapper knowledgeChunkMapper;

    private final TextChunker textChunker;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer chunkDocument(Long documentId) {
        if (documentId == null || documentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文档ID不合法");
        }

        PrdDocumentEntity document = prdDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "PRD 文档不存在");
        }

        if (!"PARSED".equals(document.getParseStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "PRD 文档尚未解析成功，当前解析状态：" + document.getParseStatus()
            );
        }

        if (!StringUtils.hasText(document.getParsedContent())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "PRD 解析内容为空，无法切片");
        }

        try {
            prdDocumentMapper.updateIndexStatus(documentId, "CHUNKING", null);

            knowledgeChunkMapper.disableByDocumentId(documentId);

            List<TextChunk> textChunks = textChunker.chunk(document.getParsedContent());
            if (textChunks.isEmpty()) {
                prdDocumentMapper.updateIndexStatus(documentId, "FAILED", "切片结果为空");
                throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ERROR, "切片结果为空");
            }

            for (TextChunk textChunk : textChunks) {
                KnowledgeChunkEntity entity = new KnowledgeChunkEntity();
                entity.setProjectId(document.getProjectId());
                entity.setDocumentId(document.getId());
                entity.setVersionName(document.getVersionName());
                entity.setChunkIndex(textChunk.getChunkIndex());
                entity.setTitle(textChunk.getTitle());
                entity.setContent(textChunk.getContent());
                entity.setTokenCount(textChunk.getTokenCount());

                entity.setChunkType(textChunk.getChunkType());
                entity.setParentChunkId(textChunk.getParentChunkId());
                entity.setSectionPath(textChunk.getSectionPath());
                entity.setModuleName(textChunk.getModuleName());
                entity.setRequirementId(textChunk.getRequirementId());
                entity.setStartPosition(textChunk.getStartPosition());
                entity.setEndPosition(textChunk.getEndPosition());
                entity.setSourceType("PRD");

                entity.setMetadata(buildMetadata(document, textChunk));

                knowledgeChunkMapper.insert(entity);
            }

            prdDocumentMapper.updateIndexStatus(documentId, "CHUNKED", null);

            return textChunks.size();
        } catch (BusinessException e) {
            prdDocumentMapper.updateIndexStatus(documentId, "FAILED", e.getMessage());
            throw e;
        } catch (Exception e) {
            prdDocumentMapper.updateIndexStatus(documentId, "FAILED", e.getMessage());
            throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ERROR, "文档切片失败：" + e.getMessage());
        }
    }

    @Override
    public List<KnowledgeChunkVO> listChunksByDocumentId(Long documentId) {
        if (documentId == null || documentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文档ID不合法");
        }

        PrdDocumentEntity document = prdDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "PRD 文档不存在");
        }

        return knowledgeChunkMapper.selectByDocumentId(documentId)
                .stream()
                .map(this::toVO)
                .toList();
    }

    private String buildMetadata(PrdDocumentEntity document, TextChunk textChunk) {
        return """
            {
              "documentId": %d,
              "projectId": %d,
              "versionName": "%s",
              "chunkIndex": %d,
              "originalFileName": "%s",
              "chunkType": "%s",
              "sectionPath": "%s",
              "moduleName": "%s",
              "requirementId": "%s",
              "startPosition": %d,
              "endPosition": %d,
              "sourceType": "PRD"
            }
            """.formatted(
                document.getId(),
                document.getProjectId(),
                escapeJson(document.getVersionName()),
                textChunk.getChunkIndex(),
                escapeJson(document.getOriginalFileName()),
                escapeJson(textChunk.getChunkType()),
                escapeJson(textChunk.getSectionPath()),
                escapeJson(textChunk.getModuleName()),
                escapeJson(textChunk.getRequirementId()),
                textChunk.getStartPosition() == null ? 0 : textChunk.getStartPosition(),
                textChunk.getEndPosition() == null ? 0 : textChunk.getEndPosition()
        );
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private KnowledgeChunkVO toVO(KnowledgeChunkEntity entity) {
        KnowledgeChunkVO vo = new KnowledgeChunkVO();
        vo.setId(entity.getId());
        vo.setProjectId(entity.getProjectId());
        vo.setDocumentId(entity.getDocumentId());
        vo.setVersionName(entity.getVersionName());
        vo.setChunkIndex(entity.getChunkIndex());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());
        vo.setTokenCount(entity.getTokenCount());

        vo.setChunkType(entity.getChunkType());
        vo.setParentChunkId(entity.getParentChunkId());
        vo.setSectionPath(entity.getSectionPath());
        vo.setModuleName(entity.getModuleName());
        vo.setRequirementId(entity.getRequirementId());
        vo.setStartPosition(entity.getStartPosition());
        vo.setEndPosition(entity.getEndPosition());
        vo.setSourceType(entity.getSourceType());
        vo.setMetadata(entity.getMetadata());

        vo.setEmbeddingModel(entity.getEmbeddingModel());
        vo.setEmbeddingStatus(entity.getEmbeddingStatus());
        vo.setEmbeddingErrorMessage(entity.getEmbeddingErrorMessage());
        vo.setEnabled(entity.getEnabled());
        vo.setCreatedAt(entity.getCreatedAt());
        return vo;
    }
}