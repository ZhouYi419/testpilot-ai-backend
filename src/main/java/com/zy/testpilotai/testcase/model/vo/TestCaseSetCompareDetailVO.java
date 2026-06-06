package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class TestCaseSetCompareDetailVO {

    /**
     * 对比任务。
     */
    private TestCaseSetCompareTaskVO task;

    /**
     * 新增用例。
     */
    private List<TestCaseSetCompareResultVO> added = new ArrayList<>();

    /**
     * 删除用例。
     */
    private List<TestCaseSetCompareResultVO> removed = new ArrayList<>();

    /**
     * 修改用例。
     */
    private List<TestCaseSetCompareResultVO> modified = new ArrayList<>();

    /**
     * 未变化用例。
     */
    private List<TestCaseSetCompareResultVO> unchanged = new ArrayList<>();
}