package com.zy.testpilotai.requirement.model.vo;

import com.zy.testpilotai.testcase.model.vo.TestCaseVO;
import lombok.Data;
import java.util.List;

@Data
public class IncrementalTestCaseGenerateResultVO {

    /**
     * 新生成的测试用例任务 ID
     */
    private String taskId;

    /**
     * 影响分析任务 ID
     */
    private String analysisTaskId;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 基线版本
     */
    private String baseVersionNo;

    /**
     * 目标版本
     */
    private String targetVersionNo;

    /**
     * 新增用例数量
     */
    private Integer caseCount;

    /**
     * 新增测试用例列表
     */
    private List<TestCaseVO> testCases;

    /**
     * 模型原始输出
     */
    private String rawModelOutput;
}