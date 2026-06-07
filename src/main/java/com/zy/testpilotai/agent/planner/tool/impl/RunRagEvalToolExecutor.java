package com.zy.testpilotai.agent.planner.tool.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutionResult;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutor;
import com.zy.testpilotai.rageval.model.dto.RagEvalRunRequest;
import com.zy.testpilotai.rageval.model.vo.RagEvalRunDetailVO;
import com.zy.testpilotai.rageval.service.RagEvalService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class RunRagEvalToolExecutor extends AbstractAgentToolExecutor implements AgentToolExecutor {

    private final RagEvalService ragEvalService;

    public RunRagEvalToolExecutor(
            ObjectMapper objectMapper,
            RagEvalService ragEvalService
    ) {
        super(objectMapper);
        this.ragEvalService = ragEvalService;
    }

    @Override
    public String toolName() {
        return "runRagEvalTool";
    }

    @Override
    public String description() {
        return "运行 RAG 评测集，输出 Recall@K、MRR、来源命中率等指标。";
    }

    @Override
    public List<String> requiredParams() {
        return List.of("datasetId");
    }

    @Override
    public AgentToolExecutionResult execute(Map<String, Object> inputParams) {
        checkRequired(inputParams, requiredParams());

        RagEvalRunRequest request = convert(inputParams, RagEvalRunRequest.class);
        RagEvalRunDetailVO result = ragEvalService.run(request);

        return AgentToolExecutionResult.success(result);
    }
}