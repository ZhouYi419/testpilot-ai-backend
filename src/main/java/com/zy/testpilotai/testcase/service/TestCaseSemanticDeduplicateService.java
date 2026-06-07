package com.zy.testpilotai.testcase.service;

import com.zy.testpilotai.testcase.model.dto.TestCaseEmbeddingBuildRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSemanticDeduplicateRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseEmbeddingBuildResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseSemanticDeduplicateResultVO;

public interface TestCaseSemanticDeduplicateService {

    /**
     * 构建测试用例 Embedding。
     */
    TestCaseEmbeddingBuildResultVO buildEmbeddings(TestCaseEmbeddingBuildRequest request);

    /**
     * 执行测试用例语义去重。
     */
    TestCaseSemanticDeduplicateResultVO deduplicate(TestCaseSemanticDeduplicateRequest request);

    /**
     * 查询语义去重任务详情。
     */
    TestCaseSemanticDeduplicateResultVO detail(String deduplicateTaskId);
}