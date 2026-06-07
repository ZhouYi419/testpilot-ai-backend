package com.zy.testpilotai.agent.planner.tool.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutionResult;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutor;
import com.zy.testpilotai.testcase.model.dto.TestCaseSemanticDeduplicateRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseSemanticDeduplicateResultVO;
import com.zy.testpilotai.testcase.service.TestCaseSemanticDeduplicateService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SemanticDeduplicateToolExecutor extends AbstractAgentToolExecutor implements AgentToolExecutor {

    private final TestCaseSemanticDeduplicateService deduplicateService;

    public SemanticDeduplicateToolExecutor(
            ObjectMapper objectMapper,
            TestCaseSemanticDeduplicateService deduplicateService
    ) {
        super(objectMapper);
        this.deduplicateService = deduplicateService;
    }

    @Override
    public String toolName() {
        return "semanticDeduplicateTool";
    }

    @Override
    public String description() {
        return "对测试用例执行 Embedding + pgvector 语义去重。";
    }

    @Override
    public List<String> requiredParams() {
        return List.of("compareScope");
    }

    @Override
    public AgentToolExecutionResult execute(Map<String, Object> inputParams) {
        checkRequired(inputParams, requiredParams());

        TestCaseSemanticDeduplicateRequest request = convert(inputParams, TestCaseSemanticDeduplicateRequest.class);
        TestCaseSemanticDeduplicateResultVO result = deduplicateService.deduplicate(request);

        return AgentToolExecutionResult.success(result);
    }
}