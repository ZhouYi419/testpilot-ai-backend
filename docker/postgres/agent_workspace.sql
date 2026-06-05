-- Agent 主任务表
CREATE TABLE IF NOT EXISTS agent_task (
                                          id BIGSERIAL PRIMARY KEY,

    -- Agent 任务 ID
                                          agent_task_id VARCHAR(64) NOT NULL UNIQUE,

    -- 工作流类型：
    -- STANDARD_TEST_DESIGN / INCREMENTAL_TEST_DESIGN / AI_APP_TEST_DESIGN
    workflow_type VARCHAR(64) NOT NULL,

    -- 项目 ID
    project_id BIGINT,

    -- 基线版本号
    base_version_no VARCHAR(64),

    -- 目标版本号
    target_version_no VARCHAR(64),

    -- 模块编码
    module_code VARCHAR(128),

    -- 用户输入目标
    user_goal TEXT NOT NULL,

    -- 新需求内容
    new_requirement TEXT,

    -- AI 应用类型
    app_type VARCHAR(64),

    -- AI 应用说明
    app_description TEXT,

    -- 选择的 Skill
    selected_skills JSONB,

    -- Agent 状态：
    -- RUNNING / SUCCESS / FAILED
    status VARCHAR(32) DEFAULT 'RUNNING',

    -- 最终结果 JSON
    final_result JSONB,

    -- 关联的影响分析任务 ID
    analysis_task_id VARCHAR(64),

    -- 关联的测试用例生成任务 ID
    testcase_task_id VARCHAR(64),

    -- 关联的 AI 应用测试任务 ID
    ai_app_task_id VARCHAR(64),

    -- 错误信息
    error_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Agent 执行步骤表
CREATE TABLE IF NOT EXISTS agent_task_step (
                                               id BIGSERIAL PRIMARY KEY,

    -- Agent 任务 ID
                                               agent_task_id VARCHAR(64) NOT NULL,

    -- 步骤序号
    step_index INT NOT NULL,

    -- 步骤名称
    step_name VARCHAR(128) NOT NULL,

    -- 步骤状态：
    -- RUNNING / SUCCESS / FAILED / SKIPPED
    status VARCHAR(32) DEFAULT 'RUNNING',

    -- 步骤输入 JSON
    input JSONB,

    -- 步骤输出 JSON
    output JSONB,

    -- 错误信息
    error_message TEXT,

    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_agent_task_agent_task_id
    ON agent_task(agent_task_id);

CREATE INDEX IF NOT EXISTS idx_agent_task_project
    ON agent_task(project_id);

CREATE INDEX IF NOT EXISTS idx_agent_task_status
    ON agent_task(status);

CREATE INDEX IF NOT EXISTS idx_agent_step_agent_task_id
    ON agent_task_step(agent_task_id);