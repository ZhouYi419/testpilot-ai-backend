-- 测试用例版本管理 / 用例集对比
-- 作用：
-- 1. 保存用例集快照，形成测试用例版本历史
-- 2. 支持两个用例集之间的差异对比
-- 3. 输出新增、删除、修改、未变化用例

CREATE TABLE IF NOT EXISTS test_case_version_history (
                                                         id BIGSERIAL PRIMARY KEY,

    -- 历史记录业务 ID
                                                         history_id VARCHAR(64) NOT NULL UNIQUE,

    -- 关联对比任务 ID，可为空
    compare_task_id VARCHAR(64),

    -- 快照类型：
    -- MANUAL_SNAPSHOT：手动快照
    -- SOURCE_SNAPSHOT：对比源用例集快照
    -- TARGET_SNAPSHOT：对比目标用例集快照
    snapshot_type VARCHAR(64) DEFAULT 'MANUAL_SNAPSHOT',

    -- 用例集业务 ID
    case_set_id VARCHAR(64),

    -- 测试用例数据库 ID
    test_case_id BIGINT,

    -- 项目 ID
    project_id BIGINT,

    -- 版本号
    version_no VARCHAR(64),

    -- 模块编码
    module_code VARCHAR(128),

    -- 用例基础字段快照
    case_title VARCHAR(512),
    case_type VARCHAR(64),
    priority VARCHAR(32),
    precondition TEXT,
    steps TEXT,
    expected_result TEXT,
    test_data TEXT,
    source_references TEXT,
    risk_point TEXT,
    automation_suggestion TEXT,

    -- 用例来源和审核状态快照
    source_type VARCHAR(64),
    review_status VARCHAR(64),

    -- 内容哈希，用于快速判断是否变更
    content_hash VARCHAR(128),

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS test_case_set_compare_task (
                                                          id BIGSERIAL PRIMARY KEY,

    -- 对比任务业务 ID
                                                          compare_task_id VARCHAR(64) NOT NULL UNIQUE,

    -- 项目 ID
    project_id BIGINT,

    -- 源用例集
    source_case_set_id VARCHAR(64) NOT NULL,

    -- 目标用例集
    target_case_set_id VARCHAR(64) NOT NULL,

    -- 状态：RUNNING / SUCCESS / FAILED
    status VARCHAR(32) DEFAULT 'RUNNING',

    -- 统计信息
    added_count INT DEFAULT 0,
    removed_count INT DEFAULT 0,
    modified_count INT DEFAULT 0,
    unchanged_count INT DEFAULT 0,

    -- 汇总 JSON
    summary JSONB,

    -- 错误信息
    error_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS test_case_set_compare_result (
                                                            id BIGSERIAL PRIMARY KEY,

    -- 对比任务业务 ID
                                                            compare_task_id VARCHAR(64) NOT NULL,

    -- 结果类型：
    -- ADDED：目标用例集中新增
    -- REMOVED：目标用例集中删除
    -- MODIFIED：目标用例集中修改
    -- UNCHANGED：未变化
    result_type VARCHAR(32) NOT NULL,

    -- 源用例 ID
    source_test_case_id BIGINT,

    -- 目标用例 ID
    target_test_case_id BIGINT,

    -- 源用例标题
    source_case_title VARCHAR(512),

    -- 目标用例标题
    target_case_title VARCHAR(512),

    -- 变更说明
    change_summary TEXT,

    -- 字段差异 JSON
    field_diffs JSONB,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_test_case_version_history_case_set
    ON test_case_version_history(case_set_id);

CREATE INDEX IF NOT EXISTS idx_test_case_version_history_case
    ON test_case_version_history(test_case_id);

CREATE INDEX IF NOT EXISTS idx_test_case_version_history_project_version
    ON test_case_version_history(project_id, version_no);

CREATE INDEX IF NOT EXISTS idx_test_case_version_history_compare_task
    ON test_case_version_history(compare_task_id);

CREATE INDEX IF NOT EXISTS idx_test_case_set_compare_task_source
    ON test_case_set_compare_task(source_case_set_id);

CREATE INDEX IF NOT EXISTS idx_test_case_set_compare_task_target
    ON test_case_set_compare_task(target_case_set_id);

CREATE INDEX IF NOT EXISTS idx_test_case_set_compare_task_status
    ON test_case_set_compare_task(status);

CREATE INDEX IF NOT EXISTS idx_test_case_set_compare_result_task
    ON test_case_set_compare_result(compare_task_id);

CREATE INDEX IF NOT EXISTS idx_test_case_set_compare_result_type
    ON test_case_set_compare_result(result_type);