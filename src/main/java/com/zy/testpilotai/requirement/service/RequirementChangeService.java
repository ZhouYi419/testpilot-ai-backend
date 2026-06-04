package com.zy.testpilotai.requirement.service;

import com.zy.testpilotai.requirement.model.dto.ChangeImpactAnalyzeRequest;
import com.zy.testpilotai.requirement.model.dto.IncrementalTestCaseGenerateRequest;
import com.zy.testpilotai.requirement.model.vo.ChangeImpactAnalyzeResultVO;
import com.zy.testpilotai.requirement.model.vo.IncrementalTestCaseGenerateResultVO;

public interface RequirementChangeService {

    /**
     * 新需求影响分析。
     */
    ChangeImpactAnalyzeResultVO analyzeImpact(ChangeImpactAnalyzeRequest request);

    /**
     * 查询影响分析任务。
     */
    ChangeImpactAnalyzeResultVO getImpactTask(String analysisTaskId);

    /**
     * 基于影响分析结果生成增量测试用例。
     */
    IncrementalTestCaseGenerateResultVO generateIncrementalCases(IncrementalTestCaseGenerateRequest request);
}