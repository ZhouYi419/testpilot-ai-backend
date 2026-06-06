package com.zy.testpilotai.llm.structured;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.common.utils.JsonExtractUtils;
import com.zy.testpilotai.llm.structured.dto.AiGeneratedTestCaseItemDTO;
import com.zy.testpilotai.llm.structured.dto.AiGeneratedTestCaseOutputDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 测试用例结构化输出解析器。
 */
@Component
@RequiredArgsConstructor
public class AiTestCaseOutputParser {

    private final ObjectMapper objectMapper;

    /**
     * 解析 AI 生成的测试用例输出。
     */
    public AiGeneratedTestCaseOutputDTO parse(String rawOutput) {
        if (!StringUtils.hasText(rawOutput)) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "AI 输出为空，无法解析测试用例"
            );
        }

        try {
            String json = JsonExtractUtils.extractJsonObject(rawOutput);
            JsonNode root = objectMapper.readTree(json);

            JsonNode testCasesNode = findTestCasesNode(root);

            if (testCasesNode == null || !testCasesNode.isArray()) {
                throw new BusinessException(
                        ErrorCode.AI_OUTPUT_PARSE_ERROR,
                        "AI 输出中缺少 testCases 数组"
                );
            }

            List<AiGeneratedTestCaseItemDTO> testCases = new ArrayList<>();

            for (JsonNode itemNode : testCasesNode) {
                if (!itemNode.isObject()) {
                    continue;
                }

                AiGeneratedTestCaseItemDTO item = parseItem(itemNode);
                validateItem(item);
                testCases.add(item);
            }

            if (testCases.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.AI_OUTPUT_PARSE_ERROR,
                        "AI 输出中的 testCases 为空"
                );
            }

            AiGeneratedTestCaseOutputDTO output = new AiGeneratedTestCaseOutputDTO();
            output.setTestCases(testCases);
            return output;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "解析 AI 测试用例输出失败：" + e.getMessage()
            );
        }
    }

    /**
     * 兼容不同模型可能返回的数组字段名。
     */
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

    /**
     * 解析单条用例。
     */
    private AiGeneratedTestCaseItemDTO parseItem(JsonNode node) {
        AiGeneratedTestCaseItemDTO item = new AiGeneratedTestCaseItemDTO();

        item.setModuleCode(text(node, "moduleCode"));
        item.setModuleName(text(node, "moduleName"));
        item.setCaseTitle(firstText(node, "caseTitle", "title", "name"));
        item.setCaseType(firstText(node, "caseType", "type"));
        item.setPriority(text(node, "priority"));
        item.setPrecondition(firstText(node, "precondition", "preconditions"));
        item.setExpectedResult(firstText(node, "expectedResult", "expected", "expectation"));
        item.setRiskPoint(firstText(node, "riskPoint", "risk"));
        item.setAutomationSuggestion(firstText(node, "automationSuggestion", "automation", "automationAdvice"));

        item.setSteps(jsonString(firstNode(node, "steps", "testSteps")));
        item.setTestData(jsonString(firstNode(node, "testData", "data", "inputData")));
        item.setSourceReferences(jsonString(firstNode(node, "sourceReferences", "references", "sources")));

        return item;
    }

    /**
     * 必填字段校验。
     */
    private void validateItem(AiGeneratedTestCaseItemDTO item) {
        if (!StringUtils.hasText(item.getCaseTitle())) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "测试用例标题不能为空"
            );
        }

        if (!StringUtils.hasText(item.getExpectedResult())) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "测试用例预期结果不能为空，用例标题：" + item.getCaseTitle()
            );
        }

        if (!StringUtils.hasText(item.getSteps())
                || "null".equals(item.getSteps())
                || "[]".equals(item.getSteps())) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "测试步骤不能为空，用例标题：" + item.getCaseTitle()
            );
        }

        if (!StringUtils.hasText(item.getCaseType())) {
            item.setCaseType("功能测试");
        }

        if (!StringUtils.hasText(item.getPriority())) {
            item.setPriority("P1");
        }

        if (!StringUtils.hasText(item.getTestData())) {
            item.setTestData("{}");
        }

        if (!StringUtils.hasText(item.getSourceReferences())) {
            item.setSourceReferences("[]");
        }
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);

        if (value.isMissingNode() || value.isNull()) {
            return null;
        }

        if (value.isTextual() || value.isNumber() || value.isBoolean()) {
            return value.asText();
        }

        return jsonString(value);
    }

    private String firstText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = node.path(fieldName);

            if (!value.isMissingNode() && !value.isNull()) {
                return text(node, fieldName);
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

    /**
     * 把任意 JsonNode 转成适合入库的 JSON 字符串。
     */
    private String jsonString(JsonNode node) {
        try {
            if (node == null || node.isMissingNode() || node.isNull()) {
                return null;
            }
            
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "JSON 字段转换失败：" + e.getMessage()
            );
        }
    }
}