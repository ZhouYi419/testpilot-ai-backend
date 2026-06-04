package com.zy.testpilotai.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.common.storage.FileStorageService;
import com.zy.testpilotai.document.chunk.ChunkGroup;
import com.zy.testpilotai.document.chunk.ParentChildChunker;
import com.zy.testpilotai.document.mapper.DocumentChunkMapper;
import com.zy.testpilotai.document.mapper.PrdDocumentMapper;
import com.zy.testpilotai.document.model.entity.DocumentChunk;
import com.zy.testpilotai.document.model.entity.PrdDocument;
import com.zy.testpilotai.document.model.vo.DocumentChunkVO;
import com.zy.testpilotai.document.model.vo.PrdParseResultVO;
import com.zy.testpilotai.document.parser.DocumentParser;
import com.zy.testpilotai.document.parser.DocumentParserFactory;
import com.zy.testpilotai.document.parser.ParsedDocument;
import com.zy.testpilotai.document.service.PrdParseService;
import com.zy.testpilotai.module.mapper.ProjectModuleMapper;
import com.zy.testpilotai.module.model.entity.ProjectModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PrdParseServiceImpl implements PrdParseService {

    private final PrdDocumentMapper prdDocumentMapper;

    private final DocumentChunkMapper documentChunkMapper;

    private final ProjectModuleMapper projectModuleMapper;

    private final FileStorageService fileStorageService;

    private final DocumentParserFactory documentParserFactory;

    private final ParentChildChunker parentChildChunker;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PrdParseResultVO parse(Long documentId) {
        PrdDocument document = prdDocumentMapper.selectById(documentId);

        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "PRD 文档不存在");
        }

        updateParseStatus(document, "PARSING", null);

        try {
            byte[] fileBytes = fileStorageService.read(document.getObjectName());

            DocumentParser parser = documentParserFactory.getParser(document.getDocName());
            ParsedDocument parsedDocument = parser.parse(document.getDocName(), fileBytes);

            String rawText = parsedDocument.getText();

            if (!StringUtils.hasText(rawText)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文档解析结果为空");
            }

            String moduleName = queryModuleName(document.getProjectId(), document.getModuleCode());

            documentChunkMapper.delete(
                    new LambdaQueryWrapper<DocumentChunk>()
                            .eq(DocumentChunk::getDocumentId, documentId)
            );

            List<ChunkGroup> chunkGroups = parentChildChunker.split(rawText);

            int parentCount = 0;
            int childCount = 0;

            for (ChunkGroup group : chunkGroups) {
                DocumentChunk parentChunk = buildChunk(
                        document,
                        moduleName,
                        null,
                        "PARENT",
                        group.getParent().getSectionTitle(),
                        group.getParent().getChunkIndex(),
                        group.getParent().getContent()
                );

                documentChunkMapper.insert(parentChunk);
                parentCount++;

                for (var child : group.getChildren()) {
                    DocumentChunk childChunk = buildChunk(
                            document,
                            moduleName,
                            parentChunk.getId(),
                            "CHILD",
                            child.getSectionTitle(),
                            child.getChunkIndex(),
                            child.getContent()
                    );

                    documentChunkMapper.insert(childChunk);
                    childCount++;
                }
            }

            document.setRawText(rawText);
            document.setParseStatus("PARSED");
            document.setErrorMessage(null);
            document.setUpdateTime(LocalDateTime.now());
            prdDocumentMapper.updateById(document);

            PrdParseResultVO resultVO = new PrdParseResultVO();
            resultVO.setDocumentId(documentId);
            resultVO.setParseStatus("PARSED");
            resultVO.setRawTextLength(rawText.length());
            resultVO.setParentChunkCount(parentCount);
            resultVO.setChildChunkCount(childCount);
            resultVO.setTotalChunkCount(parentCount + childCount);

            return resultVO;
        } catch (BusinessException e) {
            updateParseStatus(document, "FAILED", e.getMessage());
            throw e;
        } catch (Exception e) {
            updateParseStatus(document, "FAILED", e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "PRD 解析失败：" + e.getMessage());
        }
    }

    @Override
    public List<DocumentChunkVO> listChunks(Long documentId, String chunkType) {
        PrdDocument document = prdDocumentMapper.selectById(documentId);

        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "PRD 文档不存在");
        }

        LambdaQueryWrapper<DocumentChunk> wrapper = new LambdaQueryWrapper<DocumentChunk>()
                .eq(DocumentChunk::getDocumentId, documentId)
                .orderByAsc(DocumentChunk::getChunkType)
                .orderByAsc(DocumentChunk::getChunkIndex);

        if (StringUtils.hasText(chunkType)) {
            wrapper.eq(DocumentChunk::getChunkType, chunkType);
        }

        List<DocumentChunk> chunks = documentChunkMapper.selectList(wrapper);

        return chunks.stream().map(this::toVO).toList();
    }

    private DocumentChunk buildChunk(
            PrdDocument document,
            String moduleName,
            Long parentChunkId,
            String chunkType,
            String sectionTitle,
            Integer chunkIndex,
            String content
    ) {
        DocumentChunk chunk = new DocumentChunk();
        chunk.setProjectId(document.getProjectId());
        chunk.setDocumentId(document.getId());
        chunk.setVersionNo(document.getVersionNo());
        chunk.setModuleCode(document.getModuleCode());
        chunk.setModuleName(moduleName);
        chunk.setParentChunkId(parentChunkId);
        chunk.setChunkType(chunkType);
        chunk.setSectionTitle(sectionTitle);
        chunk.setChunkIndex(chunkIndex);
        chunk.setChangeType("UNKNOWN");
        chunk.setContent(content);
        chunk.setTokenCount(parentChildChunker.estimateTokenCount(content));
        chunk.setVectorId(null);
        chunk.setMetadata(buildMetadata(document, moduleName, chunkType, sectionTitle, chunkIndex));
        chunk.setCreateTime(LocalDateTime.now());
        return chunk;
    }

    private String buildMetadata(
            PrdDocument document,
            String moduleName,
            String chunkType,
            String sectionTitle,
            Integer chunkIndex
    ) {
        try {
            Map<String, Object> metadata = new LinkedHashMap<>();
            metadata.put("projectId", document.getProjectId());
            metadata.put("documentId", document.getId());
            metadata.put("versionNo", document.getVersionNo());
            metadata.put("moduleCode", document.getModuleCode());
            metadata.put("moduleName", moduleName);
            metadata.put("docType", document.getDocType());
            metadata.put("docName", document.getDocName());
            metadata.put("sectionTitle", sectionTitle);
            metadata.put("chunkType", chunkType);
            metadata.put("chunkIndex", chunkIndex);
            metadata.put("changeType", "UNKNOWN");

            return objectMapper.writeValueAsString(metadata);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "构建 Chunk Metadata 失败：" + e.getMessage());
        }
    }

    private String queryModuleName(Long projectId, String moduleCode) {
        if (!StringUtils.hasText(moduleCode)) {
            return null;
        }

        ProjectModule module = projectModuleMapper.selectOne(
                new LambdaQueryWrapper<ProjectModule>()
                        .eq(ProjectModule::getProjectId, projectId)
                        .eq(ProjectModule::getModuleCode, moduleCode)
                        .last("LIMIT 1")
        );

        return module == null ? null : module.getModuleName();
    }

    private void updateParseStatus(PrdDocument document, String status, String errorMessage) {
        document.setParseStatus(status);
        document.setErrorMessage(errorMessage);
        document.setUpdateTime(LocalDateTime.now());
        prdDocumentMapper.updateById(document);
    }

    private DocumentChunkVO toVO(DocumentChunk chunk) {
        DocumentChunkVO vo = new DocumentChunkVO();

        // 基础信息
        vo.setId(chunk.getId());
        vo.setProjectId(chunk.getProjectId());
        vo.setDocumentId(chunk.getDocumentId());
        vo.setVersionNo(chunk.getVersionNo());
        vo.setModuleCode(chunk.getModuleCode());
        vo.setModuleName(chunk.getModuleName());

        // Chunk 结构信息
        vo.setParentChunkId(chunk.getParentChunkId());
        vo.setChunkType(chunk.getChunkType());
        vo.setSectionTitle(chunk.getSectionTitle());
        vo.setChunkIndex(chunk.getChunkIndex());

        // Chunk 内容信息
        vo.setChangeType(chunk.getChangeType());
        vo.setContent(chunk.getContent());
        vo.setTokenCount(chunk.getTokenCount());
        vo.setVectorId(chunk.getVectorId());
        vo.setMetadata(chunk.getMetadata());
        vo.setCreateTime(chunk.getCreateTime());

        // 向量化状态信息
        vo.setEmbeddingStatus(chunk.getEmbeddingStatus());
        vo.setEmbeddingModel(chunk.getEmbeddingModel());
        vo.setEmbeddedTime(chunk.getEmbeddedTime());

        return vo;
    }
}