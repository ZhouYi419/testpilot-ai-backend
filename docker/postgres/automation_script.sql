-- 接口自动化脚本生成
-- 作用：
-- 1. 保存自动化脚本生成任务
-- 2. 保存生成出来的脚本文件内容
-- 3. 支持根据用例集 / 任务 / 指定用例生成 pytest + requests 脚本

CREATE TABLE IF NOT EXISTS automation_script_task (
                                                      id BIGSERIAL PRIMARY KEY,

    -- 脚本生成任务业务 ID
                                                      script_task_id VARCHAR(64) NOT NULL UNIQUE,

    -- 来源类型：
    -- CASE_SET：从用例集生成
    -- TASK：从测试用例生成任务生成
    -- IDS：从指定测试用例 ID 生成
    source_type VARCHAR(32) NOT NULL,

    -- 用例集 ID，可为空
    case_set_id VARCHAR(64),

    -- 测试用例生成任务 ID，可为空
    testcase_task_id VARCHAR(64),

    -- 项目 ID
    project_id BIGINT,

    -- 版本号
    version_no VARCHAR(64),

    -- 模块编码
    module_code VARCHAR(128),

    -- 脚本框架
    script_framework VARCHAR(64) DEFAULT 'PYTEST_REQUESTS',

    -- 生成模式：
    -- LLM：调用大模型生成
    -- TEMPLATE：兜底模板生成
    generate_mode VARCHAR(32) DEFAULT 'LLM',

    -- 基础请求地址，例如 http://localhost:8080
    base_url VARCHAR(512),

    -- 鉴权类型：
    -- NONE / BEARER_TOKEN / CUSTOM_HEADER
    auth_type VARCHAR(64) DEFAULT 'NONE',

    -- 鉴权 Header 名称，例如 Authorization / X-Token
    auth_header_name VARCHAR(128),

    -- Token 占位符，例如 ${API_TOKEN}
    token_placeholder VARCHAR(255),

    -- 公共 Header JSON
    common_headers JSONB,

    -- 选中的测试用例 ID JSON
    selected_case_ids JSONB,

    -- 状态：RUNNING / SUCCESS / FAILED
    status VARCHAR(32) DEFAULT 'RUNNING',

    -- 测试用例数量
    case_count INT DEFAULT 0,

    -- 生成文件数量
    file_count INT DEFAULT 0,

    -- 模型原始输出
    raw_model_output TEXT,

    -- 错误信息
    error_message TEXT,

    -- 汇总 JSON
    summary JSONB,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS automation_script_file (
                                                      id BIGSERIAL PRIMARY KEY,

    -- 脚本生成任务业务 ID
                                                      script_task_id VARCHAR(64) NOT NULL,

    -- 文件路径，例如 tests/test_login.py
    file_path VARCHAR(512) NOT NULL,

    -- 文件类型：
    -- PYTHON / TEXT / CONFIG / MARKDOWN
    file_type VARCHAR(64) DEFAULT 'TEXT',

    -- 文件说明
    description TEXT,

    -- 文件内容
    file_content TEXT NOT NULL,

    -- 文件大小
    file_size INT DEFAULT 0,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_automation_script_task_source_type
    ON automation_script_task(source_type);

CREATE INDEX IF NOT EXISTS idx_automation_script_task_case_set
    ON automation_script_task(case_set_id);

CREATE INDEX IF NOT EXISTS idx_automation_script_task_testcase_task
    ON automation_script_task(testcase_task_id);

CREATE INDEX IF NOT EXISTS idx_automation_script_task_project_version
    ON automation_script_task(project_id, version_no);

CREATE INDEX IF NOT EXISTS idx_automation_script_task_status
    ON automation_script_task(status);

CREATE INDEX IF NOT EXISTS idx_automation_script_file_task
    ON automation_script_file(script_task_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_automation_script_file_task_path
    ON automation_script_file(script_task_id, file_path);