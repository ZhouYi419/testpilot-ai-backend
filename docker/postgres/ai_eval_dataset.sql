-- AI 应用测试数据集管理
-- 作用：
-- 1. 管理 AI 应用测试数据集
-- 2. 管理 Prompt 注入、幻觉、RAG、知识越权、Agent 工具调用等测试样本

CREATE TABLE IF NOT EXISTS ai_eval_dataset (
                                               id BIGSERIAL PRIMARY KEY,

    -- 数据集业务 ID
                                               dataset_id VARCHAR(64) NOT NULL UNIQUE,

    -- 项目 ID，可为空
    project_id BIGINT,

    -- 版本号，可为空
    version_no VARCHAR(64),

    -- 模块编码，可为空
    module_code VARCHAR(128),

    -- 数据集名称
    dataset_name VARCHAR(255) NOT NULL,

    -- 数据集类型：
    -- RAG：RAG 应用测试集
    -- LLM：普通 LLM 应用测试集
    -- AGENT：Agent 应用测试集
    -- PROMPT：Prompt 测试集
    -- SAFETY：安全测试集
    -- MIXED：混合测试集
    dataset_type VARCHAR(64) DEFAULT 'MIXED',

    -- 描述
    description TEXT,

    -- 样本数量
    case_count INT DEFAULT 0,

    -- 状态：ACTIVE / DELETED
    status VARCHAR(32) DEFAULT 'ACTIVE',

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS ai_eval_case (
                                            id BIGSERIAL PRIMARY KEY,

    -- 样本业务 ID
                                            case_id VARCHAR(64) NOT NULL UNIQUE,

    -- 数据集业务 ID
    dataset_id VARCHAR(64) NOT NULL,

    -- 测试类型：
    -- RAG_QA：RAG 问答准确性
    -- RAG_SOURCE_CITATION：来源引用
    -- HALLUCINATION：幻觉
    -- PROMPT_INJECTION：Prompt 注入
    -- KNOWLEDGE_ACCESS_CONTROL：知识越权
    -- AGENT_TOOL_CALL：Agent 工具调用
    -- OUTPUT_FORMAT：输出格式
    -- CONSISTENCY：一致性
    -- REFUSAL：拒答能力
    case_type VARCHAR(64) NOT NULL,

    -- 测试维度：
    -- ACCURACY / SECURITY / STABILITY / COST / PERFORMANCE / FORMAT / TOOL_USE
    test_dimension VARCHAR(64),

    -- 样本名称
    case_name VARCHAR(255) NOT NULL,

    -- 用户输入 / Prompt
    input_text TEXT NOT NULL,

    -- 上下文，可为空
    context_text TEXT,

    -- 期望行为
    expected_behavior TEXT,

    -- 标准答案
    expected_answer TEXT,

    -- 期望关键词 JSON 数组
    expected_keywords JSONB,

    -- 禁止出现的关键词 JSON 数组
    forbidden_keywords JSONB,

    -- 期望工具名称，Agent 测试用
    expected_tool_name VARCHAR(128),

    -- 期望来源 JSON，RAG 测试用
    expected_sources JSONB,

    -- 期望输出格式，例如 JSON / MARKDOWN / TEXT
    expected_output_format VARCHAR(64),

    -- 风险等级：LOW / MEDIUM / HIGH / CRITICAL
    risk_level VARCHAR(32) DEFAULT 'MEDIUM',

    -- 标签 JSON 数组
    tags JSONB,

    -- 状态：ACTIVE / DELETED
    status VARCHAR(32) DEFAULT 'ACTIVE',

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_ai_eval_dataset_project_version
    ON ai_eval_dataset(project_id, version_no);

CREATE INDEX IF NOT EXISTS idx_ai_eval_dataset_type
    ON ai_eval_dataset(dataset_type);

CREATE INDEX IF NOT EXISTS idx_ai_eval_dataset_status
    ON ai_eval_dataset(status);

CREATE INDEX IF NOT EXISTS idx_ai_eval_case_dataset
    ON ai_eval_case(dataset_id);

CREATE INDEX IF NOT EXISTS idx_ai_eval_case_type
    ON ai_eval_case(case_type);

CREATE INDEX IF NOT EXISTS idx_ai_eval_case_dimension
    ON ai_eval_case(test_dimension);

CREATE INDEX IF NOT EXISTS idx_ai_eval_case_risk
    ON ai_eval_case(risk_level);

CREATE INDEX IF NOT EXISTS idx_ai_eval_case_status
    ON ai_eval_case(status);