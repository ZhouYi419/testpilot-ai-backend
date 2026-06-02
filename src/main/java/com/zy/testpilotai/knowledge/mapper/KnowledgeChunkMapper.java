package com.zy.testpilotai.knowledge.mapper;

import com.zy.testpilotai.knowledge.model.entity.KnowledgeChunkEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识片段 Mapper
 */
@Mapper
public interface KnowledgeChunkMapper {

    int insert(KnowledgeChunkEntity chunk);

    List<KnowledgeChunkEntity> selectByDocumentId(@Param("documentId") Long documentId);

    List<KnowledgeChunkEntity> selectEnabledWithoutEmbeddingByDocumentId(@Param("documentId") Long documentId);

    List<KnowledgeChunkEntity> searchSimilarChunks(
            @Param("projectId") Long projectId,
            @Param("versionName") String versionName,
            @Param("queryEmbedding") String queryEmbedding,
            @Param("topK") Integer topK
    );

    int updateEmbeddingStatus(
            @Param("id") Long id,
            @Param("embeddingStatus") String embeddingStatus,
            @Param("embeddingErrorMessage") String embeddingErrorMessage
    );

    int updateEmbedding(
            @Param("id") Long id,
            @Param("embedding") String embedding,
            @Param("embeddingModel") String embeddingModel,
            @Param("embeddingStatus") String embeddingStatus,
            @Param("embeddingErrorMessage") String embeddingErrorMessage
    );

    int disableByDocumentId(@Param("documentId") Long documentId);

    long countEnabledByDocumentId(@Param("documentId") Long documentId);
}