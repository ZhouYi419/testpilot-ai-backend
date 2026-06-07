package com.zy.testpilotai.agent.planner.tool.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutionResult;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutor;
import com.zy.testpilotai.aieval.model.dto.AiEvalRunRequest;
import com.zy.testpilotai.aieval.model.vo.AiEvalRunDetailVO;
import com.zy.testpilotai.aieval.service.AiEvalRunnerService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RunAiEvalToolExecutor extends AbstractAgentToolExecutor implements AgentToolExecutor {

    private final AiEvalRunnerService aiEvalRunnerService;

    public RunAiEvalToolExecutor(
            ObjectMapper objectMapper,
            AiEvalRunnerService aiEvalRunnerService
    ) {
        super(objectMapper);
        this.aiEvalRunnerService = aiEvalRunnerService;
    }

    @Override
    public String toolName() {
        return "runAiEvalTool";
    }

    @Override
    public String description() {
        return "运行 AI 应用测试数据集，评估准确性、安全性、格式稳定性、幻觉、Prompt 注入等指标。";
    }

    @Override
    public List<String> requiredParams() {
        return List.of("datasetId", "appConfigId");
    }

    @Override
    public AgentToolExecutionResult execute(Map<String, Object> inputParams) {
        checkRequired(inputParams, requiredParams());

        AiEvalRunRequest request = convert(inputParams, AiEvalRunRequest.class);
        AiEvalRunDetailVO result = aiEvalRunnerService.run(request);

        return AgentToolExecutionResult.success(result);
    }
}