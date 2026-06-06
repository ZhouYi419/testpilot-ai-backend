--测试用例人工编辑 / 采纳 / 驳回 / 删除

ALTER TABLE test_case
    ADD COLUMN IF NOT EXISTS review_status VARCHAR(32) DEFAULT 'AI_GENERATED';

ALTER TABLE test_case
    ADD COLUMN IF NOT EXISTS reviewer VARCHAR(64);

ALTER TABLE test_case
    ADD COLUMN IF NOT EXISTS review_time TIMESTAMP;

ALTER TABLE test_case
    ADD COLUMN IF NOT EXISTS manual_comment TEXT;

ALTER TABLE test_case
    ADD COLUMN IF NOT EXISTS accepted_time TIMESTAMP;

ALTER TABLE test_case
    ADD COLUMN IF NOT EXISTS rejected_time TIMESTAMP;

UPDATE test_case
SET review_status = 'AI_GENERATED'
WHERE review_status IS NULL;

CREATE INDEX IF NOT EXISTS idx_test_case_review_status
    ON test_case(review_status);

CREATE INDEX IF NOT EXISTS idx_test_case_task_review_status
    ON test_case(task_id, review_status);

CREATE INDEX IF NOT EXISTS idx_test_case_project_version_review_status
    ON test_case(project_id, version_no, review_status);