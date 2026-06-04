-- 测试用例生成任务表
CREATE TABLE IF NOT EXISTS testcase_generate_task (
                                                      id BIGSERIAL PRIMARY KEY,
                                                      task_id VARCHAR(64) NOT NULL UNIQUE,
    project_id BIGINT NOT NULL,
    version_no VARCHAR(64),
    module_code VARCHAR(128),
    generate_goal TEXT NOT NULL,
    generate_type VARCHAR(64) DEFAULT 'FULL',
    selected_skills JSONB,
    status VARCHAR(32) DEFAULT 'PENDING',
    raw_model_output TEXT,
    quality_score NUMERIC(5,2),
    error_message TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- 测试用例表
CREATE TABLE IF NOT EXISTS test_case (
                                         id BIGSERIAL PRIMARY KEY,
                                         task_id VARCHAR(64) NOT NULL,
    project_id BIGINT NOT NULL,
    version_no VARCHAR(64),
    module_code VARCHAR(128),
    module_name VARCHAR(128),
    case_title VARCHAR(255) NOT NULL,
    case_type VARCHAR(64),
    priority VARCHAR(16),
    precondition TEXT,
    steps JSONB,
    expected_result TEXT,
    test_data JSONB,
    source_references JSONB,
    risk_point TEXT,
    automation_suggestion TEXT,
    quality_score NUMERIC(5,2),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_testcase_task_id ON test_case(task_id);
CREATE INDEX IF NOT EXISTS idx_testcase_project_id ON test_case(project_id);
CREATE INDEX IF NOT EXISTS idx_testcase_version ON test_case(version_no);
CREATE INDEX IF NOT EXISTS idx_testcase_module ON test_case(module_code);
CREATE INDEX IF NOT EXISTS idx_testcase_generate_task_project ON testcase_generate_task(project_id);