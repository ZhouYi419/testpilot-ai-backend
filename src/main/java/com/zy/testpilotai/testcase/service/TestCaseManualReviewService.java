package com.zy.testpilotai.testcase.service;

import com.zy.testpilotai.testcase.model.dto.TestCaseManualQueryRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseReviewStatusRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseUpdateRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseManualVO;
import java.util.List;

public interface TestCaseManualReviewService {

    /**
     * 查询人工管理用例列表。
     */
    List<TestCaseManualVO> list(TestCaseManualQueryRequest request);

    /**
     * 人工编辑测试用例。
     */
    TestCaseManualVO update(TestCaseUpdateRequest request);

    /**
     * 采纳测试用例。
     */
    Boolean accept(TestCaseReviewStatusRequest request);

    /**
     * 驳回测试用例。
     */
    Boolean reject(TestCaseReviewStatusRequest request);

    /**
     * 删除测试用例。
     */
    Boolean delete(TestCaseReviewStatusRequest request);
}