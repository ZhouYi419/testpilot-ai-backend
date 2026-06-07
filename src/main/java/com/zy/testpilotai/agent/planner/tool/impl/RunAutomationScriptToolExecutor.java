package com.zy.testpilotai.agent.planner.tool.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutionResult;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutor;
import com.zy.testpilotai.automation.model.dto.AutomationRunStartRequest;
import com.zy.testpilotai.automation.model.vo.AutomationRunTaskVO;
import com.zy.testpilotai.automation.service.AutomationRunService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RunAutomationScriptToolExecutor extends AbstractAgentToolExecutor implements AgentToolExecutor {

    private final AutomationRunService automationRunService;

    public RunAutomationScriptToolExecutor(
            ObjectMapper objectMapper,
            AutomationRunService automationRunService
    ) {
        super(objectMapper);
        this.automationRunService = automationRunService;
    }

    @Override
    public String toolName() {
        return "runAutomationScriptTool";
    }

    @Override
    public String description() {
        return "启动 pytest 自动化脚本执行任务。";
    }

    @Override
    public List<String> requiredParams() {
        return List.of("scriptTaskId");
    }

    @Override
    public AgentToolExecutionResult execute(Map<String, Object> inputParams) {
        checkRequired(inputParams, requiredParams());

        AutomationRunStartRequest request = convert(inputParams, AutomationRunStartRequest.class);
        AutomationRunTaskVO result = automationRunService.start(request);

        return AgentToolExecutionResult.success(result);
    }
}