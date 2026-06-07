package com.zy.testpilotai.agent.planner.tool.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutionResult;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutor;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeEvaluateRequest;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeEvaluateResultVO;
import com.zy.testpilotai.knowledge.service.KnowledgeEvaluateService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EvaluateKnowledgeToolExecutor extends AbstractAgentToolExecutor implements AgentToolExecutor {

    private final KnowledgeEvaluateService knowledgeEvaluateService;

    public EvaluateKnowledgeToolExecutor(
            ObjectMapper objectMapper,
            KnowledgeEvaluateService knowledgeEvaluateService
    ) {
        super(objectMapper);
        this.knowledgeEvaluateService = knowledgeEvaluateService;
    }

    @Override
    public String toolName() {
        return "evaluateKnowledgeTool";
    }

    @Override
    public String description() {
        return "评估知识库质量，检查文档、Chunk、Embedding、模块、版本、查询命中等指标。";
    }

    @Override
    public List<String> requiredParams() {
        return List.of("projectId");
    }

    @Override
    public AgentToolExecutionResult execute(Map<String, Object> inputParams) {
        checkRequired(inputParams, requiredParams());

        KnowledgeEvaluateRequest request = convert(inputParams, KnowledgeEvaluateRequest.class);
        KnowledgeEvaluateResultVO result = knowledgeEvaluateService.evaluate(request);

        return AgentToolExecutionResult.success(result);
    }
}