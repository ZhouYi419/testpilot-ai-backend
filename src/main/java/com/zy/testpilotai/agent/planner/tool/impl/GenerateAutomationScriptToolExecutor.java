package com.zy.testpilotai.agent.planner.tool.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutionResult;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutor;
import com.zy.testpilotai.automation.model.dto.AutomationScriptGenerateRequest;
import com.zy.testpilotai.automation.model.vo.AutomationScriptDetailVO;
import com.zy.testpilotai.automation.service.AutomationScriptService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GenerateAutomationScriptToolExecutor extends AbstractAgentToolExecutor implements AgentToolExecutor {

    private final AutomationScriptService automationScriptService;

    public GenerateAutomationScriptToolExecutor(
            ObjectMapper objectMapper,
            AutomationScriptService automationScriptService
    ) {
        super(objectMapper);
        this.automationScriptService = automationScriptService;
    }

    @Override
    public String toolName() {
        return "generateAutomationScriptTool";
    }

    @Override
    public String description() {
        return "根据用例集、测试用例任务或指定用例 ID 生成 pytest + requests 自动化脚本。";
    }

    @Override
    public List<String> requiredParams() {
        return List.of("sourceType");
    }

    @Override
    public AgentToolExecutionResult execute(Map<String, Object> inputParams) {
        checkRequired(inputParams, requiredParams());

        AutomationScriptGenerateRequest request = convert(inputParams, AutomationScriptGenerateRequest.class);
        AutomationScriptDetailVO result = automationScriptService.generate(request);

        return AgentToolExecutionResult.success(result);
    }
}