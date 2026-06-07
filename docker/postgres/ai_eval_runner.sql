-- AI 应用测试执行器
-- 作用：
-- 1. 配置待测 AI 应用接口
-- 2. 批量执行 AI 应用测试数据集
-- 3. 保存每条样本请求、响应、评分、风险结果
-- 4. 输出准确性、安全性、格式稳定性、平均耗时等指标

CREATE TABLE IF NOT EXISTS ai_eval_app_config (
                                                  id BIGSERIAL PRIMARY KEY,

    -- 待测 AI 应用配置业务 ID
                                                  app_config_id VARCHAR(64) NOT NULL UNIQUE,

    -- 配置名称
    config_name VARCHAR(255) NOT NULL,

    -- 应用类型：
    -- RAG / LLM / AGENT / PROMPT / MIXED
    app_type VARCHAR(64) DEFAULT 'MIXED',

    -- 接口地址
    endpoint_url VARCHAR(1024) NOT NULL,

    -- HTTP 方法：POST / GET
    http_method VARCHAR(16) DEFAULT 'POST',

    -- 鉴权类型：
    -- NONE / BEARER_TOKEN / CUSTOM_HEADER
    auth_type VARCHAR(64) DEFAULT 'NONE',

    -- Header 名称，例如 Authorization / X-API-Key
    auth_header_name VARCHAR(128),

    -- API Key，开发阶段明文保存，企业级建议加密
    api_key TEXT,

    -- 固定请求 Header JSON
    headers JSONB,

    -- 请求体模板
    -- 推荐格式：
    -- {
    --   "input": {{inputText}},
    --   "context": {{contextText}}
    -- }
    request_body_template TEXT,

    -- 响应中模型输出字段路径，例如 answer / data.answer / choices.0.message.content
    response_json_path VARCHAR(255),

    -- 超时时间，单位秒
    timeout_seconds INT DEFAULT 120,

    -- 描述
    description TEXT,

    -- 状态：ACTIVE / DELETED
    status VARCHAR(32) DEFAULT 'ACTIVE',

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS ai_eval_run (
                                           id BIGSERIAL PRIMARY KEY,

    -- 运行任务业务 ID
                                           run_id VARCHAR(64) NOT NULL UNIQUE,

    -- 数据集业务 ID
    dataset_id VARCHAR(64) NOT NULL,

    -- 待测应用配置 ID
    app_config_id VARCHAR(64) NOT NULL,

    project_id BIGINT,
    version_no VARCHAR(64),
    module_code VARCHAR(128),

    -- 状态：RUNNING / SUCCESS / FAILED
    status VARCHAR(32) DEFAULT 'RUNNING',

    -- 样本总数
    total_case_count INT DEFAULT 0,

    -- 通过样本数
    passed_case_count INT DEFAULT 0,

    -- 失败样本数
    failed_case_count INT DEFAULT 0,

    -- 调用异常数
    error_count INT DEFAULT 0,

    -- 平均分
    avg_score DOUBLE PRECISION DEFAULT 0,

    -- 准确性通过率
    accuracy_pass_rate DOUBLE PRECISION DEFAULT 0,

    -- 安全通过率
    security_pass_rate DOUBLE PRECISION DEFAULT 0,

    -- 格式通过率
    format_pass_rate DOUBLE PRECISION DEFAULT 0,

    -- 平均响应耗时
    avg_latency_ms DOUBLE PRECISION DEFAULT 0,

    -- Prompt 注入攻击成功数
    prompt_injection_success_count INT DEFAULT 0,

    -- 幻觉风险数
    hallucination_count INT DEFAULT 0,

    -- 知识越权风险数
    knowledge_leak_count INT DEFAULT 0,

    -- 汇总 JSON
    summary JSONB,

    error_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_time TIMESTAMP,
    end_time TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS ai_eval_result (
                                              id BIGSERIAL PRIMARY KEY,

    -- 运行任务业务 ID
                                              run_id VARCHAR(64) NOT NULL,

    -- 样本业务 ID
    case_id VARCHAR(64) NOT NULL,

    dataset_id VARCHAR(64) NOT NULL,
    app_config_id VARCHAR(64) NOT NULL,

    -- 测试类型快照
    case_type VARCHAR(64),

    -- 测试维度快照
    test_dimension VARCHAR(64),

    -- 样本名称快照
    case_name VARCHAR(255),

    -- 输入快照
    input_text TEXT,

    -- 请求体
    request_payload TEXT,

    -- HTTP 状态码
    http_status INT,

    -- 原始响应体
    response_body TEXT,

    -- 提取后的模型输出
    model_output TEXT,

    -- 是否通过：1 是，0 否
    passed SMALLINT DEFAULT 0,

    -- 准确性是否通过
    accuracy_pass SMALLINT DEFAULT 0,

    -- 安全性是否通过
    security_pass SMALLINT DEFAULT 0,

    -- 格式是否通过
    format_pass SMALLINT DEFAULT 0,

    -- 工具调用是否通过
    tool_call_pass SMALLINT DEFAULT 0,

    -- 来源引用是否通过
    source_pass SMALLINT DEFAULT 0,

    -- 是否命中期望关键词
    expected_keyword_hit SMALLINT DEFAULT 0,

    -- 是否出现禁止关键词
    forbidden_keyword_hit SMALLINT DEFAULT 0,

    -- 命中的期望关键词 JSON
    matched_expected_keywords JSONB,

    -- 命中的禁止关键词 JSON
    matched_forbidden_keywords JSONB,

    -- 得分 0 - 100
    score DOUBLE PRECISION DEFAULT 0,

    -- 响应耗时
    latency_ms BIGINT DEFAULT 0,

    -- 评估说明
    evaluation_message TEXT,

    -- 错误信息
    error_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_ai_eval_app_config_type
    ON ai_eval_app_config(app_type);

CREATE INDEX IF NOT EXISTS idx_ai_eval_app_config_status
    ON ai_eval_app_config(status);

CREATE INDEX IF NOT EXISTS idx_ai_eval_run_dataset
    ON ai_eval_run(dataset_id);

CREATE INDEX IF NOT EXISTS idx_ai_eval_run_app_config
    ON ai_eval_run(app_config_id);

CREATE INDEX IF NOT EXISTS idx_ai_eval_run_project_version
    ON ai_eval_run(project_id, version_no);

CREATE INDEX IF NOT EXISTS idx_ai_eval_run_status
    ON ai_eval_run(status);

CREATE INDEX IF NOT EXISTS idx_ai_eval_result_run
    ON ai_eval_result(run_id);

CREATE INDEX IF NOT EXISTS idx_ai_eval_result_case
    ON ai_eval_result(case_id);

CREATE INDEX IF NOT EXISTS idx_ai_eval_result_case_type
    ON ai_eval_result(case_type);

CREATE INDEX IF NOT EXISTS idx_ai_eval_result_passed
    ON ai_eval_result(passed);