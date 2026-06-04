package com.zy.testpilotai.document.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zy.testpilotai.document.model.entity.DocumentChunk;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

@Mapper
public interface DocumentChunkMapper extends BaseMapper<DocumentChunk> {

    /**
     * 更新 Chunk 的向量字段。
     */
    @Update("""
            UPDATE document_chunk
            SET embedding = CAST(#{embeddingText} AS vector),
                embedding_status = 'DONE',
                embedding_model = #{embeddingModel},
                embedded_time = CURRENT_TIMESTAMP
            WHERE id = #{chunkId}
            """)
    int updateEmbedding(
            @Param("chunkId") Long chunkId,
            @Param("embeddingText") String embeddingText,
            @Param("embeddingModel") String embeddingModel
    );

    /**
     * 更新 Chunk 的向量化状态。
     * 某个 Chunk 向量生成失败时会调用这个方法。
     */
    @Update("""
            UPDATE document_chunk
            SET embedding_status = #{status},
                embedded_time = CURRENT_TIMESTAMP
            WHERE id = #{chunkId}
            """)
    int updateEmbeddingStatus(
            @Param("chunkId") Long chunkId,
            @Param("status") String status
    );

    /**
     * 基于 pgvector 做知识库相似度检索。
     *
     * 检索逻辑：
     * 1. 只检索 CHILD Chunk，因为 Child Chunk 更适合语义召回。
     * 2. 必须限制 project_id，防止跨项目召回。
     * 3. versionNo 为空时，不限制版本。
     * 4. moduleCode 为空时，不限制模块。
     * 5. 使用 cosine distance：embedding <=> queryEmbedding。
     * 6. score = 1 - distance，分数越高表示越相似。
     */
    @Select("""
            SELECT
                id,
                project_id,
                document_id,
                version_no,
                module_code,
                module_name,
                parent_chunk_id,
                chunk_type,
                section_title,
                chunk_index,
                change_type,
                content,
                token_count,
                vector_id,
                metadata::text AS metadata,
                create_time,
                embedding_status,
                embedding_model,
                embedded_time,
                1 - (embedding <=> CAST(#{embeddingText} AS vector)) AS score
            FROM document_chunk
            WHERE chunk_type = 'CHILD'
              AND embedding IS NOT NULL
              AND project_id = #{projectId}
              AND (#{versionNo} IS NULL OR version_no = #{versionNo})
              AND (#{moduleCode} IS NULL OR module_code = #{moduleCode})
            ORDER BY embedding <=> CAST(#{embeddingText} AS vector)
            LIMIT #{topK}
            """)
    List<KnowledgeSearchRow> searchByVector(
            @Param("projectId") Long projectId,
            @Param("versionNo") String versionNo,
            @Param("moduleCode") String moduleCode,
            @Param("embeddingText") String embeddingText,
            @Param("topK") Integer topK
    );
}