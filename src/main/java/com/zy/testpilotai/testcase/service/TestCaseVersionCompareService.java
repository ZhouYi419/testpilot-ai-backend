package com.zy.testpilotai.testcase.service;

import com.zy.testpilotai.testcase.model.dto.TestCaseSetCompareQueryRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetCompareRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetSnapshotRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseVersionHistoryQueryRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseSetCompareDetailVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseSetCompareTaskVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseVersionHistoryVO;
import java.util.List;

public interface TestCaseVersionCompareService {

    /**
     * 手动生成用例集快照。
     */
    List<TestCaseVersionHistoryVO> snapshot(TestCaseSetSnapshotRequest request);

    /**
     * 对比两个用例集。
     */
    TestCaseSetCompareDetailVO compare(TestCaseSetCompareRequest request);

    /**
     * 查询对比任务列表。
     */
    List<TestCaseSetCompareTaskVO> listCompareTasks(TestCaseSetCompareQueryRequest request);

    /**
     * 查询对比详情。
     */
    TestCaseSetCompareDetailVO compareDetail(String compareTaskId);

    /**
     * 查询用例版本历史。
     */
    List<TestCaseVersionHistoryVO> listHistory(TestCaseVersionHistoryQueryRequest request);
}