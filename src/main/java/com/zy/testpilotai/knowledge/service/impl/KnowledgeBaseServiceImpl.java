package com.zy.testpilotai.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.common.utils.VectorUtils;
import com.zy.testpilotai.document.mapper.DocumentChunkMapper;
import com.zy.testpilotai.document.mapper.PrdDocumentMapper;
import com.zy.testpilotai.document.model.entity.DocumentChunk;
import com.zy.testpilotai.document.model.entity.PrdDocument;
import com.zy.testpilotai.knowledge.mapper.KnowledgeBuildTaskMapper;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRow;
import com.zy.testpilotai.knowledge.model.entity.KnowledgeBuildTask;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeBuildResultVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeBuildTaskVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeSearchResultVO;
import com.zy.testpilotai.knowledge.model.vo.RagContextVO;
import com.zy.testpilotai.knowledge.service.KnowledgeBaseService;
import com.zy.testpilotai.llm.embedding.EmbeddingClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    private final PrdDocumentMapper prdDocumentMapper;

    private final DocumentChunkMapper documentChunkMapper;

    private final KnowledgeBuildTaskMapper knowledgeBuildTaskMapper;

    private final EmbeddingClient embeddingClient;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeBuildResultVO buildDocument(Long documentId) {
        PrdDocument document = prdDocumentMapper.selectById(documentId);

        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "PRD 文档不存在");
        }

        if (!"PARSED".equals(document.getParseStatus())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请先解析 PRD 文档，再构建知识库");
        }

        /*
         * 只给 CHILD Chunk 生成向量。
         * Parent Chunk 主要用于补全上下文
         */
        List<DocumentChunk> childChunks = documentChunkMapper.selectList(
                new LambdaQueryWrapper<DocumentChunk>()
                        .eq(DocumentChunk::getDocumentId, documentId)
                        .eq(DocumentChunk::getChunkType, "CHILD")
                        .orderByAsc(DocumentChunk::getChunkIndex)
        );

        if (childChunks.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该文档没有可向量化的 Child Chunk");
        }

        String taskId = "kb_" + UUID.randomUUID().toString().replace("-", "");

        KnowledgeBuildTask task = new KnowledgeBuildTask();
        task.setTaskId(taskId);
        task.setProjectId(document.getProjectId());
        task.setDocumentId(documentId);
        task.setStatus("RUNNING");
        task.setTotalChunks(childChunks.size());
        task.setSuccessChunks(0);
        task.setFailChunks(0);
        task.setStartTime(LocalDateTime.now());
        task.setCreateTime(LocalDateTime.now());

        knowledgeBuildTaskMapper.insert(task);

        int successCount = 0;
        int failCount = 0;
        StringBuilder errorMessageBuilder = new StringBuilder();

        try {
            for (DocumentChunk chunk : childChunks) {
                try {
                    List<Float> vector = embeddingClient.embed(chunk.getContent());

                    if (vector.size() != embeddingClient.dimension()) {
                        throw new BusinessException(
                                ErrorCode.SYSTEM_ERROR,
                                "Embedding 维度不正确，期望 "
                                        + embeddingClient.dimension()
                                        + "，实际 "
                                        + vector.size()
                        );
                    }

                    /*
                     * 把 Java 向量转成 pgvector 字符串格式。
                     * 例如：[0.1,0.2,0.3]
                     */
                    String embeddingText = VectorUtils.toPgVectorLiteral(vector);

                    /*
                     * 写入 PostgreSQL pgvector 字段。
                     */
                    documentChunkMapper.updateEmbedding(
                            chunk.getId(),
                            embeddingText,
                            embeddingClient.modelName()
                    );

                    successCount++;
                } catch (Exception e) {
                    /*
                     * 单个 Chunk 失败不直接中断整个任务。
                     * 记录失败数量，并把该 Chunk 状态置为 FAILED。
                     */
                    documentChunkMapper.updateEmbeddingStatus(chunk.getId(), "FAILED");
                    failCount++;

                    errorMessageBuilder
                            .append("ChunkId=")
                            .append(chunk.getId())
                            .append(" 向量化失败：")
                            .append(e.getMessage())
                            .append("\n");
                }
            }

            task.setStatus(failCount == 0 ? "SUCCESS" : "PARTIAL_SUCCESS");
            task.setSuccessChunks(successCount);
            task.setFailChunks(failCount);
            task.setErrorMessage(errorMessageBuilder.isEmpty() ? null : errorMessageBuilder.toString());
            task.setEndTime(LocalDateTime.now());
            knowledgeBuildTaskMapper.updateById(task);

            return toBuildResultVO(task);
        } catch (Exception e) {
            task.setStatus("FAILED");
            task.setSuccessChunks(successCount);
            task.setFailChunks(failCount);
            task.setErrorMessage(e.getMessage());
            task.setEndTime(LocalDateTime.now());
            knowledgeBuildTaskMapper.updateById(task);

            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "知识库构建失败：" + e.getMessage());
        }
    }

    @Override
    public KnowledgeBuildTaskVO getBuildTask(String taskId) {
        KnowledgeBuildTask task = knowledgeBuildTaskMapper.selectOne(
                new LambdaQueryWrapper<KnowledgeBuildTask>()
                        .eq(KnowledgeBuildTask::getTaskId, taskId)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "知识库构建任务不存在");
        }

        return toTaskVO(task);
    }

    @Override
    public List<KnowledgeSearchResultVO> search(KnowledgeSearchRequest request) {
        Integer topK = normalizeTopK(request.getTopK());

        String versionNo = StringUtils.hasText(request.getVersionNo())
                ? request.getVersionNo()
                : null;

        String moduleCode = StringUtils.hasText(request.getModuleCode())
                ? request.getModuleCode()
                : null;

        /*
         * 对用户 query 生成向量。
         */
        List<Float> queryVector = embeddingClient.embed(request.getQuery());
        String embeddingText = VectorUtils.toPgVectorLiteral(queryVector);

        List<KnowledgeSearchRow> rows = documentChunkMapper.searchByVector(
                request.getProjectId(),
                versionNo,
                moduleCode,
                embeddingText,
                topK
        );

        return rows.stream().map(this::toSearchResultVOWithParent).toList();
    }

    @Override
    public RagContextVO buildRagContext(KnowledgeSearchRequest request) {
        List<KnowledgeSearchResultVO> references = search(request);

        StringBuilder contextBuilder = new StringBuilder();

        contextBuilder.append("以下是从项目知识库中召回的需求资料，请只基于这些资料进行测试设计，不要编造未出现的业务规则。\n\n");

        for (int i = 0; i < references.size(); i++) {
            KnowledgeSearchResultVO item = references.get(i);

            contextBuilder.append("【资料 ")
                    .append(i + 1)
                    .append("】\n");

            contextBuilder.append("版本：")
                    .append(nullToEmpty(item.getVersionNo()))
                    .append("\n");

            contextBuilder.append("模块：")
                    .append(nullToEmpty(item.getModuleName()))
                    .append("(")
                    .append(nullToEmpty(item.getModuleCode()))
                    .append(")\n");

            contextBuilder.append("章节：")
                    .append(nullToEmpty(item.getSectionTitle()))
                    .append("\n");

            contextBuilder.append("相似度分数：")
                    .append(item.getScore())
                    .append("\n");

            if (StringUtils.hasText(item.getParentContent())) {
                contextBuilder.append("完整上下文：\n")
                        .append(item.getParentContent())
                        .append("\n\n");
            } else {
                contextBuilder.append("命中内容：\n")
                        .append(item.getContent())
                        .append("\n\n");
            }
        }

        RagContextVO vo = new RagContextVO();
        vo.setQuery(request.getQuery());
        vo.setProjectId(request.getProjectId());
        vo.setVersionNo(request.getVersionNo());
        vo.setModuleCode(request.getModuleCode());
        vo.setContextText(contextBuilder.toString());
        vo.setReferences(references);

        return vo;
    }

    private Integer normalizeTopK(Integer topK) {
        if (topK == null || topK <= 0) {
            return 5;
        }

        if (topK > 30) {
            return 30;
        }

        return topK;
    }

    private KnowledgeBuildResultVO toBuildResultVO(KnowledgeBuildTask task) {
        KnowledgeBuildResultVO vo = new KnowledgeBuildResultVO();

        vo.setTaskId(task.getTaskId());
        vo.setDocumentId(task.getDocumentId());
        vo.setProjectId(task.getProjectId());
        vo.setStatus(task.getStatus());
        vo.setTotalChunks(task.getTotalChunks());
        vo.setSuccessChunks(task.getSuccessChunks());
        vo.setFailChunks(task.getFailChunks());
        vo.setErrorMessage(task.getErrorMessage());

        return vo;
    }

    private KnowledgeBuildTaskVO toTaskVO(KnowledgeBuildTask task) {
        KnowledgeBuildTaskVO vo = new KnowledgeBuildTaskVO();

        vo.setTaskId(task.getTaskId());
        vo.setProjectId(task.getProjectId());
        vo.setDocumentId(task.getDocumentId());
        vo.setStatus(task.getStatus());
        vo.setTotalChunks(task.getTotalChunks());
        vo.setSuccessChunks(task.getSuccessChunks());
        vo.setFailChunks(task.getFailChunks());
        vo.setErrorMessage(task.getErrorMessage());
        vo.setStartTime(task.getStartTime());
        vo.setEndTime(task.getEndTime());
        vo.setCreateTime(task.getCreateTime());

        return vo;
    }

    private KnowledgeSearchResultVO toSearchResultVOWithParent(KnowledgeSearchRow row) {
        KnowledgeSearchResultVO vo = new KnowledgeSearchResultVO();

        vo.setChunkId(row.getId());
        vo.setProjectId(row.getProjectId());
        vo.setDocumentId(row.getDocumentId());
        vo.setVersionNo(row.getVersionNo());
        vo.setModuleCode(row.getModuleCode());
        vo.setModuleName(row.getModuleName());
        vo.setSectionTitle(row.getSectionTitle());
        vo.setContent(row.getContent());
        vo.setScore(row.getScore());
        vo.setMetadata(row.getMetadata());
        vo.setEmbeddingModel(row.getEmbeddingModel());
        vo.setEmbeddedTime(row.getEmbeddedTime());
        vo.setParentChunkId(row.getParentChunkId());

        /*
         * 根据 parentChunkId 查询 Parent Chunk。
         */
        if (row.getParentChunkId() != null) {
            DocumentChunk parent = documentChunkMapper.selectById(row.getParentChunkId());

            if (parent != null) {
                vo.setParentSectionTitle(parent.getSectionTitle());
                vo.setParentContent(parent.getContent());
            }
        }

        return vo;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}