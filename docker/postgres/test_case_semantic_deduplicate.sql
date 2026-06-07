-- 测试用例语义去重升级
-- 作用：
-- 1. 给测试用例生成 Embedding
-- 2. 使用 pgvector 存储测试用例向量
-- 3. 支持测试用例语义相似度查询
-- 4. 保存语义去重任务和语义重复结果

CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS test_case_embedding (
                                                   id BIGSERIAL PRIMARY KEY,

    -- 测试用例数据库 ID
                                                   test_case_id BIGINT NOT NULL UNIQUE,

    -- 测试用例生成任务 ID
                                                   task_id VARCHAR(64),

    -- 项目 ID
    project_id BIGINT,

    -- 版本号
    version_no VARCHAR(64),

    -- 模块编码
    module_code VARCHAR(128),

    -- 用例标题快照
    case_title VARCHAR(512),

    -- 用于生成向量的文本内容哈希
    content_hash VARCHAR(128),

    -- Embedding 模型名称
    embedding_model VARCHAR(128),

    -- 向量维度
    embedding_dimension INT DEFAULT 1024,

    -- 向量数据
    embedding vector(1024),

    -- 状态：SUCCESS / FAILED
    status VARCHAR(32) DEFAULT 'SUCCESS',

    -- 错误信息
    error_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS test_case_semantic_deduplicate_task (
                                                                   id BIGSERIAL PRIMARY KEY,

    -- 语义去重任务业务 ID
                                                                   deduplicate_task_id VARCHAR(64) NOT NULL UNIQUE,

    -- 对比范围：
    -- TASK：同任务内去重
    -- VERSION：同项目同版本内去重
    -- PROJECT：同项目内去重
    -- CROSS_VERSION：同项目同模块跨版本去重
    compare_scope VARCHAR(32) DEFAULT 'TASK',

    -- 源任务 ID，可为空
    task_id VARCHAR(64),

    project_id BIGINT,

    version_no VARCHAR(64),

    module_code VARCHAR(128),

    -- 相似度阈值
    threshold DOUBLE PRECISION DEFAULT 0.85,

    -- 每条用例最多返回相似候选数
    top_k INT DEFAULT 5,

    -- 是否重建向量：1 是，0 否
    rebuild_embedding SMALLINT DEFAULT 0,

    -- 状态：RUNNING / SUCCESS / FAILED
    status VARCHAR(32) DEFAULT 'RUNNING',

    -- 源用例数量
    source_case_count INT DEFAULT 0,

    -- 比较候选数量
    candidate_case_count INT DEFAULT 0,

    -- 重复对数量
    duplicate_pair_count INT DEFAULT 0,

    -- 更新为重复状态的用例数量
    marked_duplicate_count INT DEFAULT 0,

    -- 汇总 JSON
    summary JSONB,

    error_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS test_case_semantic_duplicate_result (
                                                                   id BIGSERIAL PRIMARY KEY,

    -- 语义去重任务业务 ID
                                                                   deduplicate_task_id VARCHAR(64) NOT NULL,

    -- 源用例 ID
    source_test_case_id BIGINT NOT NULL,

    -- 相似用例 ID
    target_test_case_id BIGINT NOT NULL,

    -- 源用例标题快照
    source_case_title VARCHAR(512),

    -- 相似用例标题快照
    target_case_title VARCHAR(512),

    -- 相似度，0 - 1
    similarity DOUBLE PRECISION NOT NULL,

    -- 对比范围
    compare_scope VARCHAR(32),

    -- 是否已更新 test_case.duplicate_status：1 是，0 否
    marked_duplicate SMALLINT DEFAULT 0,

    -- 重复原因
    duplicate_reason TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_test_case_embedding_task
    ON test_case_embedding(task_id);

CREATE INDEX IF NOT EXISTS idx_test_case_embedding_project_version
    ON test_case_embedding(project_id, version_no);

CREATE INDEX IF NOT EXISTS idx_test_case_embedding_module
    ON test_case_embedding(module_code);

CREATE INDEX IF NOT EXISTS idx_test_case_embedding_status
    ON test_case_embedding(status);

-- pgvector 余弦距离索引
CREATE INDEX IF NOT EXISTS idx_test_case_embedding_vector_cosine
    ON test_case_embedding
    USING hnsw (embedding vector_cosine_ops);

CREATE INDEX IF NOT EXISTS idx_test_case_semantic_task_scope
    ON test_case_semantic_deduplicate_task(compare_scope);

CREATE INDEX IF NOT EXISTS idx_test_case_semantic_task_task_id
    ON test_case_semantic_deduplicate_task(task_id);

CREATE INDEX IF NOT EXISTS idx_test_case_semantic_task_project_version
    ON test_case_semantic_deduplicate_task(project_id, version_no);

CREATE INDEX IF NOT EXISTS idx_test_case_semantic_result_task
    ON test_case_semantic_duplicate_result(deduplicate_task_id);

CREATE INDEX IF NOT EXISTS idx_test_case_semantic_result_source
    ON test_case_semantic_duplicate_result(source_test_case_id);

CREATE INDEX IF NOT EXISTS idx_test_case_semantic_result_target
    ON test_case_semantic_duplicate_result(target_test_case_id);