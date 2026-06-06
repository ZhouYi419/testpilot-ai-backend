-- 知识库质量评估
-- 作用：
-- 1. 评估 PRD 文档是否存在
-- 2. 评估 Chunk 数量和质量
-- 3. 评估 Parent / Child Chunk 结构
-- 4. 评估 Embedding 是否生成
-- 5. 评估 module_code / version_no 覆盖情况
-- 6. 保存评估任务和评估明细

CREATE TABLE IF NOT EXISTS knowledge_evaluate_task (
                                                       id BIGSERIAL PRIMARY KEY,

    -- 评估任务业务 ID
                                                       evaluate_task_id VARCHAR(64) NOT NULL UNIQUE,

    -- 项目 ID
    project_id BIGINT NOT NULL,

    -- 版本号
    version_no VARCHAR(64),

    -- 模块编码，可为空
    module_code VARCHAR(128),

    -- 评估查询，可为空
    query_text TEXT,

    -- 状态：RUNNING / SUCCESS / FAILED
    status VARCHAR(32) DEFAULT 'RUNNING',

    -- 总评分，0 - 100
    total_score DOUBLE PRECISION DEFAULT 0,

    -- 文档数量
    document_count INT DEFAULT 0,

    -- Chunk 总数
    chunk_count INT DEFAULT 0,

    -- Parent Chunk 数量
    parent_chunk_count INT DEFAULT 0,

    -- Child Chunk 数量
    child_chunk_count INT DEFAULT 0,

    -- Embedding 缺失数量
    embedding_missing_count INT DEFAULT 0,

    -- 模块缺失数量
    module_missing_count INT DEFAULT 0,

    -- 过短 Chunk 数量
    too_short_chunk_count INT DEFAULT 0,

    -- 过长 Chunk 数量
    too_long_chunk_count INT DEFAULT 0,

    -- 孤儿 Child Chunk 数量
    orphan_child_chunk_count INT DEFAULT 0,

    -- 评估摘要 JSON
    summary JSONB,

    -- 错误信息
    error_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS knowledge_evaluate_item (
                                                       id BIGSERIAL PRIMARY KEY,

    -- 评估任务业务 ID
                                                       evaluate_task_id VARCHAR(64) NOT NULL,

    -- 评估维度
    dimension VARCHAR(64) NOT NULL,

    -- 指标名称
    metric_name VARCHAR(128) NOT NULL,

    -- 指标值
    metric_value TEXT,

    -- 状态：PASS / WARN / FAIL
    status VARCHAR(32) NOT NULL,

    -- 得分，0 - 100
    score DOUBLE PRECISION DEFAULT 0,

    -- 问题说明
    problem TEXT,

    -- 修复建议
    suggestion TEXT,

    -- 详情 JSON
    detail JSONB,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_knowledge_evaluate_task_project_version
    ON knowledge_evaluate_task(project_id, version_no);

CREATE INDEX IF NOT EXISTS idx_knowledge_evaluate_task_status
    ON knowledge_evaluate_task(status);

CREATE INDEX IF NOT EXISTS idx_knowledge_evaluate_task_create_time
    ON knowledge_evaluate_task(create_time);

CREATE INDEX IF NOT EXISTS idx_knowledge_evaluate_item_task
    ON knowledge_evaluate_item(evaluate_task_id);

CREATE INDEX IF NOT EXISTS idx_knowledge_evaluate_item_dimension
    ON knowledge_evaluate_item(dimension);

CREATE INDEX IF NOT EXISTS idx_knowledge_evaluate_item_status
    ON knowledge_evaluate_item(status);