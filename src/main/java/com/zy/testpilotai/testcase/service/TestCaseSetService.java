package com.zy.testpilotai.testcase.service;

import com.zy.testpilotai.testcase.model.dto.TestCaseSetAddCasesRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetBuildFromAcceptedRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetCreateRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetDeleteRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetQueryRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetRemoveCasesRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetUpdateRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseSetDetailVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseSetVO;
import java.util.List;

public interface TestCaseSetService {

    /**
     * 创建用例集。
     */
    TestCaseSetVO create(TestCaseSetCreateRequest request);

    /**
     * 修改用例集。
     */
    TestCaseSetVO update(TestCaseSetUpdateRequest request);

    /**
     * 查询用例集列表。
     */
    List<TestCaseSetVO> list(TestCaseSetQueryRequest request);

    /**
     * 查询用例集详情。
     */
    TestCaseSetDetailVO detail(String caseSetId);

    /**
     * 添加测试用例到用例集。
     */
    TestCaseSetDetailVO addCases(TestCaseSetAddCasesRequest request);

    /**
     * 从用例集中移除测试用例。
     */
    TestCaseSetDetailVO removeCases(TestCaseSetRemoveCasesRequest request);

    /**
     * 从已采纳测试用例自动构建用例集。
     */
    TestCaseSetDetailVO buildFromAccepted(TestCaseSetBuildFromAcceptedRequest request);

    /**
     * 删除用例集。
     */
    Boolean delete(TestCaseSetDeleteRequest request);
}