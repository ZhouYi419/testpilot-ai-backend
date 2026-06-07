package com.zy.testpilotai.agent.planner.tool.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutionResult;
import com.zy.testpilotai.agent.planner.tool.AgentToolExecutor;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetCompareRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseSetCompareDetailVO;
import com.zy.testpilotai.testcase.service.TestCaseVersionCompareService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class CompareCaseSetToolExecutor extends AbstractAgentToolExecutor implements AgentToolExecutor {

    private final TestCaseVersionCompareService compareService;

    public CompareCaseSetToolExecutor(
            ObjectMapper objectMapper,
            TestCaseVersionCompareService compareService
    ) {
        super(objectMapper);
        this.compareService = compareService;
    }

    @Override
    public String toolName() {
        return "compareCaseSetTool";
    }

    @Override
    public String description() {
        return "对比两个测试用例集，输出新增、删除、修改、未变化用例。";
    }

    @Override
    public List<String> requiredParams() {
        return List.of("sourceCaseSetId", "targetCaseSetId");
    }

    @Override
    public AgentToolExecutionResult execute(Map<String, Object> inputParams) {
        checkRequired(inputParams, requiredParams());

        TestCaseSetCompareRequest request = convert(inputParams, TestCaseSetCompareRequest.class);
        TestCaseSetCompareDetailVO result = compareService.compare(request);

        return AgentToolExecutionResult.success(result);
    }
}