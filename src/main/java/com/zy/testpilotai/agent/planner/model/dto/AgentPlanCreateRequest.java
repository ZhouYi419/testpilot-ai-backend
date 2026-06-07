package com.zy.testpilotai.agent.planner.model.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AgentPlanCreateRequest {

    /**
     * 项目 ID。
     */
    private Long projectId;

    /**
     * 版本号。
     */
    private String versionNo;

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 用户目标。
     */
    private String userGoal;

    /**
     * 规划模式：
     * LLM / TEMPLATE。
     */
    private String planningMode;

    /**
     * 允许使用的工具。
     * 不传则默认允许全部内置工具。
     */
    private List<String> allowedTools;

    /**
     * 上下文参数。
     * 例如 datasetId、appConfigId、caseSetId、scriptTaskId、taskId 等。
     */
    private Map<String, Object> context;

    /**
     * 是否规划后自动执行。
     * 默认 false，推荐先人工确认。
     */
    private Boolean autoExecute;
}