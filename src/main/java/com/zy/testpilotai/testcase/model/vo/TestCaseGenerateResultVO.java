package com.zy.testpilotai.testcase.model.vo;

import com.zy.testpilotai.knowledge.model.vo.KnowledgeSearchResultVO;
import lombok.Data;
import java.util.List;

@Data
public class TestCaseGenerateResultVO {

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 生成的测试用例数量
     */
    private Integer caseCount;

    /**
     * 测试用例列表
     */
    private List<TestCaseVO> testCases;

    /**
     * 本次生成使用的知识库来源。
     */
    private List<KnowledgeSearchResultVO> references;

    /**
     * 模型原始输出。
     */
    private String rawModelOutput;
}