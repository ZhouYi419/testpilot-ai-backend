-- Embedding 调用日志表
CREATE TABLE IF NOT EXISTS embedding_call_log (
                                                  id BIGSERIAL PRIMARY KEY,

    -- 调用来源：
    -- KNOWLEDGE_CHUNK_EMBEDDING / RAG_QUERY_EMBEDDING / TESTCASE_EMBEDDING 等
                                                  biz_type VARCHAR(64),

    -- 业务 ID，例如 documentId / chunkId / taskId
    biz_id VARCHAR(128),

    -- 模型供应商：MOCK / DASHSCOPE / SPRING_AI
    provider VARCHAR(64),

    -- 模型名称
    model_name VARCHAR(128),

    -- 调用状态：SUCCESS / FAILED
    status VARCHAR(32),

    -- 输入文本
    input_text TEXT,

    -- 向量维度
    embedding_dimension INT,

    -- 错误信息
    error_message TEXT,

    -- 耗时，毫秒
    duration_ms BIGINT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_embedding_call_biz
    ON embedding_call_log(biz_type, biz_id);

CREATE INDEX IF NOT EXISTS idx_embedding_call_provider
    ON embedding_call_log(provider);

CREATE INDEX IF NOT EXISTS idx_embedding_call_status
    ON embedding_call_log(status);

CREATE INDEX IF NOT EXISTS idx_embedding_call_create_time
    ON embedding_call_log(create_time);