package com.zy.testpilotai.agent.planner.tool.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutionResult;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutor;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.vo.RagContextVO;
import com.zy.testpilotai.knowledge.service.KnowledgeBaseService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SearchKnowledgeToolExecutor extends AbstractAgentToolExecutor implements AgentToolExecutor {

    private final KnowledgeBaseService knowledgeBaseService;

    public SearchKnowledgeToolExecutor(
            ObjectMapper objectMapper,
            KnowledgeBaseService knowledgeBaseService
    ) {
        super(objectMapper);
        this.knowledgeBaseService = knowledgeBaseService;
    }

    @Override
    public String toolName() {
        return "searchKnowledgeTool";
    }

    @Override
    public String description() {
        return "根据 projectId、versionNo、moduleCode、query、topK 检索项目知识库并构建 RAG 上下文。";
    }

    @Override
    public List<String> requiredParams() {
        return List.of("projectId", "query");
    }

    @Override
    public AgentToolExecutionResult execute(Map<String, Object> inputParams) {
        checkRequired(inputParams, requiredParams());

        KnowledgeSearchRequest request = convert(inputParams, KnowledgeSearchRequest.class);
        RagContextVO result = knowledgeBaseService.buildRagContext(request);

        return AgentToolExecutionResult.success(result);
    }
}