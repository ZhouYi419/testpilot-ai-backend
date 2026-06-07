-- 自动化执行引擎
-- 作用：
-- 1. 保存自动化执行任务
-- 2. 保存 pytest 执行结果
-- 3. 支持 JUnit XML 结果解析
-- 4. 支持执行日志查询和取消任务

CREATE TABLE IF NOT EXISTS automation_run_task (
                                                   id BIGSERIAL PRIMARY KEY,

    -- 自动化执行任务业务 ID
                                                   run_task_id VARCHAR(64) NOT NULL UNIQUE,

    -- 脚本生成任务业务 ID
    script_task_id VARCHAR(64) NOT NULL,

    -- 项目 ID
    project_id BIGINT,

    -- 版本号
    version_no VARCHAR(64),

    -- 模块编码
    module_code VARCHAR(128),

    -- 执行环境名称，例如 local / test / staging
    environment_name VARCHAR(64) DEFAULT 'local',

    -- 执行模式：
    -- LOCAL_PROCESS：本地进程执行
    -- DOCKER_SANDBOX：后续预留
    execution_mode VARCHAR(64) DEFAULT 'LOCAL_PROCESS',

    -- 工作目录
    work_dir TEXT,

    -- 报告文件路径
    report_file_path TEXT,

    -- base url
    base_url VARCHAR(512),

    -- API Token，开发阶段可存，企业级建议加密
    api_token TEXT,

    -- 额外环境变量 JSON
    extra_env JSONB,

    -- 超时时间，单位秒
    timeout_seconds INT DEFAULT 600,

    -- 是否请求取消：0 否，1 是
    cancel_requested SMALLINT DEFAULT 0,

    -- 状态：
    -- PENDING / RUNNING / SUCCESS / FAILED / CANCELLED
    status VARCHAR(32) DEFAULT 'PENDING',

    -- pytest 退出码
    exit_code INT,

    -- 总数
    total_count INT DEFAULT 0,

    -- 通过数
    passed_count INT DEFAULT 0,

    -- 失败数
    failed_count INT DEFAULT 0,

    -- 错误数
    error_count INT DEFAULT 0,

    -- 跳过数
    skipped_count INT DEFAULT 0,

    -- 耗时毫秒
    duration_ms BIGINT DEFAULT 0,

    -- 标准输出
    stdout_log TEXT,

    -- 标准错误
    stderr_log TEXT,

    -- JUnit XML 原文
    junit_xml TEXT,

    -- 错误信息
    error_message TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    start_time TIMESTAMP,
    end_time TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS automation_case_result (
                                                      id BIGSERIAL PRIMARY KEY,

    -- 自动化执行任务业务 ID
                                                      run_task_id VARCHAR(64) NOT NULL,

    -- 测试类名
    class_name VARCHAR(512),

    -- 测试方法名
    case_name VARCHAR(512),

    -- 状态：
    -- PASSED / FAILED / ERROR / SKIPPED
    status VARCHAR(32),

    -- 耗时秒
    time_seconds DOUBLE PRECISION DEFAULT 0,

    -- 失败 / 错误 / 跳过说明
    message TEXT,

    -- 失败详情
    detail TEXT,

    -- system-out
    system_out TEXT,

    -- system-err
    system_err TEXT,

    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_automation_run_task_script
    ON automation_run_task(script_task_id);

CREATE INDEX IF NOT EXISTS idx_automation_run_task_project_version
    ON automation_run_task(project_id, version_no);

CREATE INDEX IF NOT EXISTS idx_automation_run_task_status
    ON automation_run_task(status);

CREATE INDEX IF NOT EXISTS idx_automation_run_task_create_time
    ON automation_run_task(create_time);

CREATE INDEX IF NOT EXISTS idx_automation_case_result_run
    ON automation_case_result(run_task_id);

CREATE INDEX IF NOT EXISTS idx_automation_case_result_status
    ON automation_case_result(status);