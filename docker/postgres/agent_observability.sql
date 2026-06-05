-- Agent 执行日志表
CREATE TABLE IF NOT EXISTS agent_execution_log (
                                                   id BIGSERIAL PRIMARY KEY,

    -- Agent 任务 ID
                                                   agent_task_id VARCHAR(64) NOT NULL,

    -- 步骤序号，可为空
    step_index INT,

    -- 步骤类型
    step_type VARCHAR(64),

    -- 步骤名称
    step_name VARCHAR(128),

    -- 日志级别：INFO / WARN / ERROR
    log_level VARCHAR(16) NOT NULL,

    -- 日志事件类型：
    -- AGENT_START / AGENT_SUCCESS / AGENT_FAILED
    -- STEP_START / STEP_SUCCESS / STEP_FAILED / STEP_SKIPPED
    -- TIMEOUT / CANCELLED
    event_type VARCHAR(64) NOT NULL,

    -- 日志消息
    message TEXT,

    -- 输入快照 JSON
    input_snapshot JSONB,

    -- 输出快照 JSON
    output_snapshot JSONB,

    -- 错误信息
    error_message TEXT,

    -- 错误堆栈
    error_stack TEXT,

    -- 模型供应商，后续 Spring AI 接入后使用
    model_provider VARCHAR(64),

    -- 模型名称，后续 Spring AI 接入后使用
    model_name VARCHAR(128),

    -- 输入 token，预留
    prompt_tokens INT,

    -- 输出 token，预留
    completion_tokens INT,

    -- 总 token，预留
    total_tokens INT,

    -- 耗时，毫秒
    duration_ms BIGINT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_agent_execution_log_task
    ON agent_execution_log(agent_task_id);

CREATE INDEX IF NOT EXISTS idx_agent_execution_log_step
    ON agent_execution_log(agent_task_id, step_index);

CREATE INDEX IF NOT EXISTS idx_agent_execution_log_event
    ON agent_execution_log(event_type);

CREATE INDEX IF NOT EXISTS idx_agent_execution_log_level
    ON agent_execution_log(log_level);