-- 新需求影响分析任务表
CREATE TABLE IF NOT EXISTS requirement_change_analysis_task (
                                                                id BIGSERIAL PRIMARY KEY,

    -- 影响分析任务 ID
                                                                analysis_task_id VARCHAR(64) NOT NULL UNIQUE,

    -- 项目 ID
    project_id BIGINT NOT NULL,

    -- 基线版本，例如 v1.0
    base_version_no VARCHAR(64) NOT NULL,

    -- 目标版本，例如 v1.1
    target_version_no VARCHAR(64) NOT NULL,

    -- 新需求内容
    new_requirement TEXT NOT NULL,

    -- 状态：RUNNING / SUCCESS / FAILED
    status VARCHAR(32) DEFAULT 'RUNNING',

    -- 影响模块 JSON
    affected_modules JSONB,

    -- 相关旧规则 JSON
    related_old_rules JSONB,

    -- 相关历史用例 JSON
    related_historical_cases JSONB,

    -- 变更摘要 JSON
    change_summary JSONB,

    -- 风险点 JSON
    risk_points JSONB,

    -- 回归范围 JSON
    regression_scope JSONB,

    -- 建议新增测试点 JSON
    suggested_new_test_points JSONB,

    -- 模型原始输出
    raw_model_output TEXT,

    -- 错误信息
    error_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_requirement_change_project
    ON requirement_change_analysis_task(project_id);

CREATE INDEX IF NOT EXISTS idx_requirement_change_base_target
    ON requirement_change_analysis_task(base_version_no, target_version_no);

CREATE INDEX IF NOT EXISTS idx_requirement_change_status
    ON requirement_change_analysis_task(status);