-- AI 应用测试生成任务表
CREATE TABLE IF NOT EXISTS ai_app_test_task (
                                                id BIGSERIAL PRIMARY KEY,

    -- AI 应用测试任务 ID
                                                task_id VARCHAR(64) NOT NULL UNIQUE,

    -- 可选：如果这个 AI 应用属于某个项目，可以绑定 project_id
    project_id BIGINT,

    -- 可选：版本号
    version_no VARCHAR(64),

    -- 可选：模块编码
    module_code VARCHAR(128),

    -- AI 应用类型：LLM / RAG / AGENT / PROMPT / AI_APP
    app_type VARCHAR(64) NOT NULL,

    -- AI 应用描述
    app_description TEXT NOT NULL,

    -- 生成目标
    generate_goal TEXT,

    -- 测试维度 JSON
    test_dimensions JSONB,

    -- 选择的 Skill JSON
    selected_skills JSONB,

    -- 任务状态：RUNNING / SUCCESS / FAILED
    status VARCHAR(32) DEFAULT 'RUNNING',

    -- 模型原始输出
    raw_model_output TEXT,

    -- 错误信息
    error_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- AI 应用测试用例表
CREATE TABLE IF NOT EXISTS ai_app_test_case (
                                                id BIGSERIAL PRIMARY KEY,

    -- 所属任务 ID
                                                task_id VARCHAR(64) NOT NULL,

    -- AI 应用类型
    app_type VARCHAR(64),

    -- 测试维度
    test_dimension VARCHAR(128),

    -- 用例标题
    case_title VARCHAR(255),

    -- 优先级
    priority VARCHAR(16),

    -- 攻击 Prompt 或输入 Prompt
    attack_prompt TEXT,

    -- 输入数据
    input_data JSONB,

    -- 前置条件
    precondition TEXT,

    -- 测试步骤
    steps JSONB,

    -- 预期行为
    expected_behavior TEXT,

    -- 通过标准
    pass_criteria TEXT,

    -- 评估方式
    evaluation_method VARCHAR(255),

    -- 风险等级
    risk_level VARCHAR(32),

    -- 自动化建议
    automation_suggestion TEXT,

    -- 来源引用
    source_references JSONB,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 如果之前已经创建过 ai_app_test_case，这里补齐新增字段
ALTER TABLE ai_app_test_case
    ADD COLUMN IF NOT EXISTS priority VARCHAR(16);

ALTER TABLE ai_app_test_case
    ADD COLUMN IF NOT EXISTS precondition TEXT;

ALTER TABLE ai_app_test_case
    ADD COLUMN IF NOT EXISTS steps JSONB;

ALTER TABLE ai_app_test_case
    ADD COLUMN IF NOT EXISTS pass_criteria TEXT;

ALTER TABLE ai_app_test_case
    ADD COLUMN IF NOT EXISTS source_references JSONB;

ALTER TABLE ai_app_test_case
    ADD COLUMN IF NOT EXISTS update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_ai_app_test_task_id
    ON ai_app_test_case(task_id);

CREATE INDEX IF NOT EXISTS idx_ai_app_test_app_type
    ON ai_app_test_case(app_type);

CREATE INDEX IF NOT EXISTS idx_ai_app_test_dimension
    ON ai_app_test_case(test_dimension);

CREATE INDEX IF NOT EXISTS idx_ai_app_test_task_project
    ON ai_app_test_task(project_id);