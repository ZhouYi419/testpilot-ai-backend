CREATE TABLE IF NOT EXISTS test_case_set (
                                             id BIGSERIAL PRIMARY KEY,

    -- 用例集业务 ID
                                             case_set_id VARCHAR(64) NOT NULL UNIQUE,

    -- 项目 ID
    project_id BIGINT NOT NULL,

    -- 版本号
    version_no VARCHAR(64),

    -- 模块编码
    module_code VARCHAR(128),

    -- 用例集名称
    set_name VARCHAR(255) NOT NULL,

    -- 用例集类型：
    -- FULL：完整用例集
    -- INCREMENTAL：增量用例集
    -- REGRESSION：回归用例集
    -- AI_APP：AI 应用专项测试用例集
    -- CUSTOM：自定义用例集
    set_type VARCHAR(32) NOT NULL DEFAULT 'CUSTOM',

    -- 用例集描述
    description TEXT,

    -- 用例数量
    case_count INT DEFAULT 0,

    -- 状态：ACTIVE / DELETED
    status VARCHAR(32) DEFAULT 'ACTIVE',

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS test_case_set_item (
                                                  id BIGSERIAL PRIMARY KEY,

    -- 用例集业务 ID
                                                  case_set_id VARCHAR(64) NOT NULL,

    -- 测试用例数据库 ID
    test_case_id BIGINT NOT NULL,

    -- 用例在用例集内的排序
    item_order INT DEFAULT 0,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE UNIQUE INDEX IF NOT EXISTS uk_test_case_set_item
    ON test_case_set_item(case_set_id, test_case_id);

CREATE INDEX IF NOT EXISTS idx_test_case_set_project_version
    ON test_case_set(project_id, version_no);

CREATE INDEX IF NOT EXISTS idx_test_case_set_module
    ON test_case_set(module_code);

CREATE INDEX IF NOT EXISTS idx_test_case_set_type
    ON test_case_set(set_type);

CREATE INDEX IF NOT EXISTS idx_test_case_set_status
    ON test_case_set(status);

CREATE INDEX IF NOT EXISTS idx_test_case_set_item_set
    ON test_case_set_item(case_set_id);

CREATE INDEX IF NOT EXISTS idx_test_case_set_item_case
    ON test_case_set_item(test_case_id);