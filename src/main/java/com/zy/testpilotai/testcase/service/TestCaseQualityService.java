package com.zy.testpilotai.testcase.service;

import com.zy.testpilotai.testcase.model.dto.MissingCaseCompleteRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseReviewRequest;
import com.zy.testpilotai.testcase.model.vo.MissingCaseCompleteResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseQualityReviewResultVO;

public interface TestCaseQualityService {

    /**
     * 对某个测试用例生成任务下的用例做质量评审。
     */
    TestCaseQualityReviewResultVO review(TestCaseReviewRequest request);

    /**
     * 根据质量评审结果补全缺失用例。
     */
    MissingCaseCompleteResultVO completeMissing(MissingCaseCompleteRequest request);
}