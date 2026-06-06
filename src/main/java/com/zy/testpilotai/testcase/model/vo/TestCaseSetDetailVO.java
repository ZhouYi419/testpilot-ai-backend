package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class TestCaseSetDetailVO {

    /**
     * 用例集基础信息
     */
    private TestCaseSetVO caseSet;

    /**
     * 用例集中的测试用例明细列表
     */
    private List<TestCaseSetItemVO> cases = new ArrayList<>();
}