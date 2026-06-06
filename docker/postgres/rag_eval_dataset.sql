-- RAG 评测集
-- 作用：
-- 1. 管理 RAG 标准问题集
-- 2. 管理标准问题、标准答案、期望关键词、期望模块、期望版本
-- 3. 批量执行 RAG 检索评测
-- 4. 计算 Recall@K / MRR / 来源命中率 / 平均得分

CREATE TABLE IF NOT EXISTS rag_eval_dataset (
                                                id BIGSERIAL PRIMARY KEY,

    -- 评测集业务 ID
                                                dataset_id VARCHAR(64) NOT NULL UNIQUE,

    -- 项目 ID
    project_id BIGINT NOT NULL,

    -- 版本号
    version_no VARCHAR(64),

    -- 模块编码
    module_code VARCHAR(128),

    -- 评测集名称
    dataset_name VARCHAR(255) NOT NULL,

    -- 评测集描述
    description TEXT,

    -- 状态：ACTIVE / DELETED
    status VARCHAR(32) DEFAULT 'ACTIVE',

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS rag_eval_question (
                                                 id BIGSERIAL PRIMARY KEY,

    -- 问题业务 ID
                                                 question_id VARCHAR(64) NOT NULL UNIQUE,

    -- 评测集业务 ID
    dataset_id VARCHAR(64) NOT NULL,

    -- 问题文本
    question_text TEXT NOT NULL,

    -- 标准答案
    standard_answer TEXT,

    -- 期望关键词 JSON 数组
    expected_keywords JSONB,

    -- 期望命中的 Chunk ID JSON 数组，可为空
    expected_chunk_ids JSONB,

    -- 期望命中的文档 ID JSON 数组，可为空
    expected_document_ids JSONB,

    -- 期望模块编码，可为空
    expected_module_code VARCHAR(128),

    -- 期望版本号，可为空
    expected_version_no VARCHAR(64),

    -- 难度：EASY / MEDIUM / HARD
    difficulty VARCHAR(32) DEFAULT 'MEDIUM',

    -- 状态：ACTIVE / DELETED
    status VARCHAR(32) DEFAULT 'ACTIVE',

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS rag_eval_run (
                                            id BIGSERIAL PRIMARY KEY,

    -- 运行任务业务 ID
                                            run_id VARCHAR(64) NOT NULL UNIQUE,

    -- 评测集业务 ID
    dataset_id VARCHAR(64) NOT NULL,

    project_id BIGINT NOT NULL,

    version_no VARCHAR(64),

    module_code VARCHAR(128),

    -- 召回数量
    top_k INT DEFAULT 5,

    -- 状态：RUNNING / SUCCESS / FAILED
    status VARCHAR(32) DEFAULT 'RUNNING',

    -- 总问题数
    total_questions INT DEFAULT 0,

    -- 命中问题数
    hit_count INT DEFAULT 0,

    -- Recall@K
    recall_at_k DOUBLE PRECISION DEFAULT 0,

    -- MRR
    mrr DOUBLE PRECISION DEFAULT 0,

    -- 来源命中率
    source_hit_rate DOUBLE PRECISION DEFAULT 0,

    -- 平均得分
    avg_score DOUBLE PRECISION DEFAULT 0,

    -- 汇总 JSON
    summary JSONB,

    error_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS rag_eval_result (
                                               id BIGSERIAL PRIMARY KEY,

    -- 运行任务业务 ID
                                               run_id VARCHAR(64) NOT NULL,

    -- 问题业务 ID
    question_id VARCHAR(64) NOT NULL,

    -- 问题文本快照
    question_text TEXT,

    -- 标准答案快照
    standard_answer TEXT,

    -- 期望关键词快照
    expected_keywords JSONB,

    -- 检索上下文 JSON
    retrieved_context JSONB,

    -- 是否命中：1 是，0 否
    hit SMALLINT DEFAULT 0,

    -- 首次命中排名，从 1 开始；未命中为 0
    hit_rank INT DEFAULT 0,

    -- 是否来源命中：1 是，0 否
    source_hit SMALLINT DEFAULT 0,

    -- 命中的关键词 JSON 数组
    matched_keywords JSONB,

    -- 得分，0 - 100
    score DOUBLE PRECISION DEFAULT 0,

    -- 评估说明
    evaluation_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_rag_eval_dataset_project_version
    ON rag_eval_dataset(project_id, version_no);

CREATE INDEX IF NOT EXISTS idx_rag_eval_dataset_status
    ON rag_eval_dataset(status);

CREATE INDEX IF NOT EXISTS idx_rag_eval_question_dataset
    ON rag_eval_question(dataset_id);

CREATE INDEX IF NOT EXISTS idx_rag_eval_question_status
    ON rag_eval_question(status);

CREATE INDEX IF NOT EXISTS idx_rag_eval_run_dataset
    ON rag_eval_run(dataset_id);

CREATE INDEX IF NOT EXISTS idx_rag_eval_run_project_version
    ON rag_eval_run(project_id, version_no);

CREATE INDEX IF NOT EXISTS idx_rag_eval_run_status
    ON rag_eval_run(status);

CREATE INDEX IF NOT EXISTS idx_rag_eval_result_run
    ON rag_eval_result(run_id);

CREATE INDEX IF NOT EXISTS idx_rag_eval_result_question
    ON rag_eval_result(question_id);

CREATE INDEX IF NOT EXISTS idx_rag_eval_result_hit
    ON rag_eval_result(hit);