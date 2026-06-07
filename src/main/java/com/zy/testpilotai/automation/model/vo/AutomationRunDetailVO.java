package com.zy.testpilotai.automation.model.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AutomationRunDetailVO {

    /**
     * 执行任务。
     */
    private AutomationRunTaskVO task;

    /**
     * 单用例执行结果。
     */
    private List<AutomationCaseResultVO> caseResults = new ArrayList<>();
}