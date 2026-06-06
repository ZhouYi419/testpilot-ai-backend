package com.zy.testpilotai.llm.structured;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.common.utils.JsonExtractUtils;
import com.zy.testpilotai.llm.structured.dto.AiAppTestCaseItemDTO;
import com.zy.testpilotai.llm.structured.dto.AiAppTestCaseOutputDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AiAppTestCaseOutputParser {

    private final ObjectMapper objectMapper;

    public AiAppTestCaseOutputDTO parse(String rawOutput) {
        if (!StringUtils.hasText(rawOutput)) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "AI 应用测试用例输出为空，无法解析"
            );
        }

        try {
            String json = JsonExtractUtils.extractJsonObject(rawOutput);
            JsonNode root = objectMapper.readTree(json);

            JsonNode testCasesNode = findTestCasesNode(root);

            if (testCasesNode == null || !testCasesNode.isArray()) {
                throw new BusinessException(
                        ErrorCode.AI_OUTPUT_PARSE_ERROR,
                        "AI 应用测试输出中缺少 testCases 数组"
                );
            }

            List<AiAppTestCaseItemDTO> cases = new ArrayList<>();

            for (JsonNode itemNode : testCasesNode) {
                if (!itemNode.isObject()) {
                    continue;
                }

                AiAppTestCaseItemDTO item = parseItem(itemNode);
                validateAndNormalize(item);
                cases.add(item);
            }

            if (cases.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.AI_OUTPUT_PARSE_ERROR,
                        "AI 应用测试输出中的 testCases 为空"
                );
            }

            AiAppTestCaseOutputDTO output = new AiAppTestCaseOutputDTO();
            output.setTestCases(cases);
            return output;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "解析 AI 应用测试用例输出失败：" + e.getMessage()
            );
        }
    }

    private JsonNode findTestCasesNode(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return null;
        }

        if (root.path("testCases").isArray()) {
            return root.path("testCases");
        }

        if (root.path("cases").isArray()) {
            return root.path("cases");
        }

        if (root.path("data").isArray()) {
            return root.path("data");
        }

        if (root.path("result").path("testCases").isArray()) {
            return root.path("result").path("testCases");
        }

        return null;
    }

    private AiAppTestCaseItemDTO parseItem(JsonNode node) {
        AiAppTestCaseItemDTO item = new AiAppTestCaseItemDTO();

        item.setAppType(firstText(node, "appType", "applicationType"));
        item.setTestDimension(firstText(node, "testDimension", "dimension", "testType"));
        item.setCaseTitle(firstText(node, "caseTitle", "title", "name"));
        item.setPriority(firstText(node, "priority", "casePriority"));
        item.setAttackPrompt(firstText(node, "attackPrompt", "prompt", "inputPrompt"));
        item.setPrecondition(firstText(node, "precondition", "preconditions"));
        item.setExpectedBehavior(firstText(node, "expectedBehavior", "expectedResult", "expected"));
        item.setPassCriteria(firstText(node, "passCriteria", "criteria"));
        item.setEvaluationMethod(firstText(node, "evaluationMethod", "evalMethod", "evaluation"));
        item.setRiskLevel(firstText(node, "riskLevel", "risk"));
        item.setAutomationSuggestion(firstText(node, "automationSuggestion", "automation", "automationAdvice"));

        item.setInputData(jsonString(firstNode(node, "inputData", "testData", "data")));
        item.setSteps(jsonString(firstNode(node, "steps", "testSteps")));
        item.setSourceReferences(jsonString(firstNode(node, "sourceReferences", "references", "sources")));

        return item;
    }

    private void validateAndNormalize(AiAppTestCaseItemDTO item) {
        if (!StringUtils.hasText(item.getCaseTitle())) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "AI 应用测试用例标题不能为空"
            );
        }

        if (!StringUtils.hasText(item.getTestDimension())) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "AI 应用测试维度不能为空，用例标题：" + item.getCaseTitle()
            );
        }

        if (!StringUtils.hasText(item.getExpectedBehavior())) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "AI 应用测试预期行为不能为空，用例标题：" + item.getCaseTitle()
            );
        }

        if (!StringUtils.hasText(item.getPriority())) {
            item.setPriority("P1");
        } else {
            item.setPriority(normalizePriority(item.getPriority()));
        }

        if (!StringUtils.hasText(item.getRiskLevel())) {
            item.setRiskLevel("MEDIUM");
        } else {
            item.setRiskLevel(normalizeRiskLevel(item.getRiskLevel()));
        }

        if (!StringUtils.hasText(item.getInputData())) {
            item.setInputData("{}");
        }

        if (!StringUtils.hasText(item.getSteps())
                || "null".equals(item.getSteps())) {
            item.setSteps("[]");
        }

        if (!StringUtils.hasText(item.getSourceReferences())
                || "null".equals(item.getSourceReferences())) {
            item.setSourceReferences("[]");
        }
    }

    private String normalizePriority(String priority) {
        String value = priority.trim().toUpperCase();

        if (value.contains("P0") || value.contains("最高") || value.contains("高危")) {
            return "P0";
        }

        if (value.contains("P1") || value.contains("高")) {
            return "P1";
        }

        if (value.contains("P2") || value.contains("中")) {
            return "P2";
        }

        if (value.contains("P3") || value.contains("低")) {
            return "P3";
        }

        return "P1";
    }

    private String normalizeRiskLevel(String riskLevel) {
        String value = riskLevel.trim().toUpperCase();

        if (value.contains("HIGH") || value.contains("高")) {
            return "HIGH";
        }

        if (value.contains("LOW") || value.contains("低")) {
            return "LOW";
        }

        return "MEDIUM";
    }

    private String firstText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = node.path(fieldName);

            if (!value.isMissingNode() && !value.isNull()) {
                return text(value);
            }
        }

        return null;
    }

    private JsonNode firstNode(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = node.path(fieldName);

            if (!value.isMissingNode() && !value.isNull()) {
                return value;
            }
        }

        return null;
    }

    private String text(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }

        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
            return node.asText();
        }

        return jsonString(node);
    }

    private String jsonString(JsonNode node) {
        try {
            if (node == null || node.isMissingNode() || node.isNull()) {
                return null;
            }

            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "AI 应用测试 JSON 字段转换失败：" + e.getMessage()
            );
        }
    }
}