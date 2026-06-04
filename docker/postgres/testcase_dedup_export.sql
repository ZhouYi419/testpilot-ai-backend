-- 给测试用例表增加去重相关字段

-- 是否重复：
-- NORMAL：正常用例
-- DUPLICATE：重复用例
ALTER TABLE test_case
    ADD COLUMN IF NOT EXISTS duplicate_status VARCHAR(32) DEFAULT 'NORMAL';

-- 如果当前用例是重复用例，则记录它重复的是哪条用例
ALTER TABLE test_case
    ADD COLUMN IF NOT EXISTS duplicate_of_case_id BIGINT;

-- 重复相似度分数
ALTER TABLE test_case
    ADD COLUMN IF NOT EXISTS duplicate_score NUMERIC(5,4);

-- 重复原因
ALTER TABLE test_case
    ADD COLUMN IF NOT EXISTS duplicate_reason TEXT;

-- 用例来源类型：
-- AI_GENERATED：AI 首次生成
-- AI_COMPLETED：AI 补全生成
-- MANUAL：人工新增
ALTER TABLE test_case
    ADD COLUMN IF NOT EXISTS source_type VARCHAR(64) DEFAULT 'AI_GENERATED';

CREATE INDEX IF NOT EXISTS idx_testcase_duplicate_status
    ON test_case(duplicate_status);

CREATE INDEX IF NOT EXISTS idx_testcase_duplicate_of
    ON test_case(duplicate_of_case_id);