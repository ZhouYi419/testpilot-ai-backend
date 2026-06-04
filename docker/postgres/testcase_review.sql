-- 测试用例质量评审任务表
CREATE TABLE IF NOT EXISTS testcase_quality_review_task (
                                                            id BIGSERIAL PRIMARY KEY,

    -- 评审任务 ID
                                                            review_task_id VARCHAR(64) NOT NULL UNIQUE,

    -- 被评审的测试用例生成任务 ID
    source_task_id VARCHAR(64) NOT NULL,

    -- 项目 ID
    project_id BIGINT NOT NULL,

    -- 版本号
    version_no VARCHAR(64),

    -- 模块编码
    module_code VARCHAR(128),

    -- 评审状态：RUNNING / SUCCESS / FAILED
    status VARCHAR(32) DEFAULT 'RUNNING',

    -- 总评分
    total_score NUMERIC(5,2),

    -- 评审结果 JSON
    review_result JSONB,

    -- 缺失测试点 JSON
    missing_points JSONB,

    -- 建议补全方向 JSON
    suggested_case_directions JSONB,

    -- 模型原始输出
    raw_model_output TEXT,

    -- 错误信息
    error_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_review_source_task_id
    ON testcase_quality_review_task(source_task_id);

CREATE INDEX IF NOT EXISTS idx_review_project_id
    ON testcase_quality_review_task(project_id);