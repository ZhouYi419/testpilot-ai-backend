package com.zy.testpilotai.testcase.service;

import com.zy.testpilotai.testcase.model.dto.TestCaseGenerateRequest;
import com.zy.testpilotai.testcase.model.dto.TestCasePageRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseGenerateResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseVO;

import java.util.List;

public interface TestCaseGenerateService {

    /**
     * 基于知识库生成测试用例。
     */
    TestCaseGenerateResultVO generate(TestCaseGenerateRequest request);

    /**
     * 查询测试用例列表。
     */
    List<TestCaseVO> list(TestCasePageRequest request);
}