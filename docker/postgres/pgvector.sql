-- 开启 pgvector 扩展，如果已存在则不会重复创建
CREATE EXTENSION IF NOT EXISTS vector;

-- 给 document_chunk 表增加向量字段
ALTER TABLE document_chunk
    ADD COLUMN IF NOT EXISTS embedding vector(1024);

-- 向量生成状态
-- PENDING：待生成
-- DONE：已生成
-- FAILED：生成失败
ALTER TABLE document_chunk
    ADD COLUMN IF NOT EXISTS embedding_status VARCHAR(32) DEFAULT 'PENDING';

-- 记录使用的 Embedding 模型名称
ALTER TABLE document_chunk
    ADD COLUMN IF NOT EXISTS embedding_model VARCHAR(128);

-- 记录向量生成时间
ALTER TABLE document_chunk
    ADD COLUMN IF NOT EXISTS embedded_time TIMESTAMP;

-- 普通查询索引：按项目、版本、模块过滤时会用到
CREATE INDEX IF NOT EXISTS idx_chunk_project_version_module
    ON document_chunk(project_id, version_no, module_code);

-- 向量索引：用于提升 pgvector 相似度检索性能
CREATE INDEX IF NOT EXISTS idx_document_chunk_embedding_hnsw
    ON document_chunk
    USING hnsw (embedding vector_cosine_ops)
    WHERE embedding IS NOT NULL;