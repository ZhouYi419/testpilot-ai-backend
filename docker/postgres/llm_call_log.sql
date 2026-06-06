-- LLM 调用日志表
CREATE TABLE IF NOT EXISTS llm_call_log (
                                            id BIGSERIAL PRIMARY KEY,

    -- 调用来源：
    -- TESTCASE_GENERATE / TESTCASE_REVIEW / REQUIREMENT_IMPACT / AI_APP_TEST / AGENT 等
                                            biz_type VARCHAR(64),

    -- 业务任务 ID
    biz_id VARCHAR(128),

    -- 模型供应商：MOCK / DASHSCOPE / SPRING_AI
    provider VARCHAR(64),

    -- 模型名称
    model_name VARCHAR(128),

    -- 调用状态：SUCCESS / FAILED
    status VARCHAR(32),

    -- 系统提示词
    system_prompt TEXT,

    -- 用户提示词
    user_prompt TEXT,

    -- 模型原始输出
    response_text TEXT,

    -- 错误信息
    error_message TEXT,

    -- 输入 token，后续补充
    prompt_tokens INT,

    -- 输出 token，后续补充
    completion_tokens INT,

    -- 总 token，后续补充
    total_tokens INT,

    -- 耗时，毫秒
    duration_ms BIGINT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_llm_call_biz
    ON llm_call_log(biz_type, biz_id);

CREATE INDEX IF NOT EXISTS idx_llm_call_provider
    ON llm_call_log(provider);

CREATE INDEX IF NOT EXISTS idx_llm_call_status
    ON llm_call_log(status);

CREATE INDEX IF NOT EXISTS idx_llm_call_create_time
    ON llm_call_log(create_time);