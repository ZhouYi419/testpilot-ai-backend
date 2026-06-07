package com.zy.testpilotai.agent.planner.parser;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.agent.planner.model.dto.AiAgentPlanOutputDTO;
import com.zy.testpilotai.agent.planner.model.dto.AiAgentPlanStepDTO;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.common.utils.JsonExtractUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AgentPlanOutputParser {

    private final ObjectMapper objectMapper;

    /**
     * 解析 AI 输出的 Agent 计划。
     */
    public AiAgentPlanOutputDTO parse(String rawOutput) {
        if (!StringUtils.hasText(rawOutput)) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "Agent Planner 输出为空"
            );
        }

        try {
            String json = JsonExtractUtils.extractJsonObject(rawOutput);
            JsonNode root = objectMapper.readTree(json);

            AiAgentPlanOutputDTO output = new AiAgentPlanOutputDTO();
            output.setPlanName(text(root, "planName", "执行计划"));
            output.setPlanDescription(text(root, "planDescription", ""));

            JsonNode stepsNode = root.path("steps");

            if (!stepsNode.isArray() || stepsNode.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.AI_OUTPUT_PARSE_ERROR,
                        "Agent Planner 输出中 steps 为空"
                );
            }

            for (JsonNode stepNode : stepsNode) {
                AiAgentPlanStepDTO step = new AiAgentPlanStepDTO();

                step.setStepName(text(stepNode, "stepName", "未命名步骤"));
                step.setToolName(text(stepNode, "toolName", ""));
                step.setStepGoal(text(stepNode, "stepGoal", ""));

                JsonNode inputParamsNode = stepNode.path("inputParams");

                Map<String, Object> inputParams = new LinkedHashMap<>();

                if (inputParamsNode.isObject()) {
                    inputParams = objectMapper.convertValue(
                            inputParamsNode,
                            objectMapper.getTypeFactory().constructMapType(
                                    LinkedHashMap.class,
                                    String.class,
                                    Object.class
                            )
                    );
                }

                step.setInputParams(inputParams);
                output.getSteps().add(step);
            }

            return output;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "解析 Agent Planner 输出失败：" + e.getMessage()
            );
        }
    }

    private String text(JsonNode node, String fieldName, String defaultValue) {
        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return defaultValue;
        }

        return value.asText(defaultValue);
    }
}