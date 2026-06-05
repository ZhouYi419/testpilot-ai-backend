package com.zy.testpilotai.agent.model.vo;

import lombok.Data;

@Data
public class AgentRunResultVO {

    /**
     * Agent 任务 ID
     */
    private String agentTaskId;

    /**
     * 工作流类型
     */
    private String workflowType;

    /**
     * 状态
     */
    private String status;

    /**
     * 影响分析任务 ID
     */
    private String analysisTaskId;

    /**
     * 测试用例生成任务 ID
     */
    private String testcaseTaskId;

    /**
     * AI 应用测试任务 ID
     */
    private String aiAppTaskId;

    /**
     * 最终结果 JSON
     */
    private String finalResult;

    /**
     * 错误信息
     */
    private String errorMessage;
}