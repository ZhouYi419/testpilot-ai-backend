-- Agent Planner / Tool Calling
-- 作用：
-- 1. 保存 Agent 规划任务
-- 2. 保存 Agent 规划步骤
-- 3. 支持 AI 规划、后端白名单校验、人工确认执行、步骤级结果追踪

CREATE TABLE IF NOT EXISTS agent_plan_task (
                                               id BIGSERIAL PRIMARY KEY,

    -- Agent 计划任务业务 ID
                                               plan_task_id VARCHAR(64) NOT NULL UNIQUE,

    -- 项目 ID
    project_id BIGINT,

    -- 版本号
    version_no VARCHAR(64),

    -- 模块编码
    module_code VARCHAR(128),

    -- 用户目标
    user_goal TEXT NOT NULL,

    -- 规划模式：LLM / TEMPLATE
    planning_mode VARCHAR(32) DEFAULT 'LLM',

    -- 允许使用的工具 JSON 数组
    allowed_tools JSONB,

    -- 原始模型输出
    raw_model_output TEXT,

    -- 最终计划 JSON
    plan_json JSONB,

    -- 状态：
    -- PLANNING / WAITING_CONFIRM / RUNNING / SUCCESS / FAILED / CANCELLED
    status VARCHAR(32) DEFAULT 'PLANNING',

    -- 是否已确认执行：0 否，1 是
    approved SMALLINT DEFAULT 0,

    -- 当前步骤序号
    current_step_index INT DEFAULT 0,

    -- 总步骤数
    total_step_count INT DEFAULT 0,

    -- 成功步骤数
    success_step_count INT DEFAULT 0,

    -- 失败步骤数
    failed_step_count INT DEFAULT 0,

    -- 最终结果 JSON
    final_result JSONB,

    -- 错误信息
    error_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_time TIMESTAMP,
    end_time TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS agent_plan_step (
                                               id BIGSERIAL PRIMARY KEY,

    -- Agent 计划任务业务 ID
                                               plan_task_id VARCHAR(64) NOT NULL,

    -- 步骤序号，从 1 开始
    step_index INT NOT NULL,

    -- 工具名称
    tool_name VARCHAR(128) NOT NULL,

    -- 步骤名称
    step_name VARCHAR(255),

    -- 步骤目标
    step_goal TEXT,

    -- 工具入参 JSON
    input_params JSONB,

    -- 状态：
    -- PENDING / RUNNING / SUCCESS / FAILED / SKIPPED
    status VARCHAR(32) DEFAULT 'PENDING',

    -- 工具输出 JSON
    output_json JSONB,

    -- 错误信息
    error_message TEXT,

    -- 重试次数
    retry_count INT DEFAULT 0,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_time TIMESTAMP,
    end_time TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_agent_plan_task_project_version
    ON agent_plan_task(project_id, version_no);

CREATE INDEX IF NOT EXISTS idx_agent_plan_task_status
    ON agent_plan_task(status);

CREATE INDEX IF NOT EXISTS idx_agent_plan_task_create_time
    ON agent_plan_task(create_time);

CREATE INDEX IF NOT EXISTS idx_agent_plan_step_task
    ON agent_plan_step(plan_task_id);

CREATE INDEX IF NOT EXISTS idx_agent_plan_step_status
    ON agent_plan_step(status);

CREATE UNIQUE INDEX IF NOT EXISTS uk_agent_plan_step_task_index
    ON agent_plan_step(plan_task_id, step_index);