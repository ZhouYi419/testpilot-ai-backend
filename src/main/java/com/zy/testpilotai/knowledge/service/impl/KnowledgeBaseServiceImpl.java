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
         * Parent Chunk 主要用于补全上下文，暂时不参与向量召回。
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

        try {
            for (DocumentChunk chunk : childChunks) {
                try {
                    /*
                     * 调用 EmbeddingClient 生成向量。
                     */
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
                }
            }

            task.setStatus(failCount == 0 ? "SUCCESS" : "PARTIAL_SUCCESS");
            task.setSuccessChunks(successCount);
            task.setFailChunks(failCount);
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
        Integer topK = request.getTopK();

        if (topK == null || topK <= 0) {
            topK = 5;
        }

        if (topK > 30) {
            topK = 30;
        }

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

        return rows.stream().map(this::toSearchResultVO).toList();
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

    private KnowledgeSearchResultVO toSearchResultVO(KnowledgeSearchRow row) {
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

        return vo;
    }
}