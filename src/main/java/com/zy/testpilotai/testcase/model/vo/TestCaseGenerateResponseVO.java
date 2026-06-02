package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class TestCaseGenerateResponseVO {

    private Long taskId;

    private Integer totalCases;

    private List<TestCaseVO> testCases;
}