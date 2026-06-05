-- Agent 异步执行增强字段

-- 执行模式：
-- ASYNC：异步执行
-- SYNC：同步执行，后续调试时可用
ALTER TABLE agent_task
    ADD COLUMN IF NOT EXISTS execution_mode VARCHAR(32) DEFAULT 'ASYNC';

-- 当前执行到第几步
ALTER TABLE agent_task
    ADD COLUMN IF NOT EXISTS current_step_index INT;

-- 是否请求取消：
-- 0：未取消
-- 1：用户请求取消
ALTER TABLE agent_task
    ADD COLUMN IF NOT EXISTS cancel_requested SMALLINT DEFAULT 0;

-- 任务开始时间
ALTER TABLE agent_task
    ADD COLUMN IF NOT EXISTS start_time TIMESTAMP;

-- 任务结束时间
ALTER TABLE agent_task
    ADD COLUMN IF NOT EXISTS end_time TIMESTAMP;

-- 重试次数
ALTER TABLE agent_task
    ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;

-- 从哪一步恢复执行
ALTER TABLE agent_task
    ADD COLUMN IF NOT EXISTS resume_from_step INT DEFAULT 1;

-- Agent 步骤增加重试次数
ALTER TABLE agent_task_step
    ADD COLUMN IF NOT EXISTS retry_count INT DEFAULT 0;

-- Agent 步骤增加是否可重试
ALTER TABLE agent_task_step
    ADD COLUMN IF NOT EXISTS retryable SMALLINT DEFAULT 1;

-- Agent 步骤增加步骤类型，方便前端展示
ALTER TABLE agent_task_step
    ADD COLUMN IF NOT EXISTS step_type VARCHAR(64);

CREATE INDEX IF NOT EXISTS idx_agent_task_cancel_requested
    ON agent_task(cancel_requested);

CREATE INDEX IF NOT EXISTS idx_agent_task_current_step
    ON agent_task(current_step_index);

CREATE INDEX IF NOT EXISTS idx_agent_step_status
    ON agent_task_step(status);