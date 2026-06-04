-- Skill 定义表
CREATE TABLE IF NOT EXISTS skill_definition (
                                                id BIGSERIAL PRIMARY KEY,

    -- Skill 编码，例如 FUNCTIONAL_TEST
                                                skill_code VARCHAR(64) NOT NULL UNIQUE,

    -- Skill 名称，例如 功能测试 Skill
    skill_name VARCHAR(128) NOT NULL,

    -- Skill 类型，例如 TEST_DESIGN / AI_APP_TEST / REVIEW
    skill_type VARCHAR(64),

    -- Skill 描述
    description TEXT,

    -- 生成测试用例时使用的 Prompt 模板
    prompt_template TEXT,

    -- 输出结构约束，JSONB
    output_schema JSONB,

    -- 质量评分规则，JSONB
    rubric JSONB,

    -- Skill 版本号
    version_no VARCHAR(32) DEFAULT '1.0.0',

    -- 是否启用：1 启用，0 禁用
    enabled SMALLINT DEFAULT 1,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_skill_code
    ON skill_definition(skill_code);

CREATE INDEX IF NOT EXISTS idx_skill_enabled
    ON skill_definition(enabled);