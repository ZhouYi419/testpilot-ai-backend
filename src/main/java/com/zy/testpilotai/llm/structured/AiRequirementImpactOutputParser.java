package com.zy.testpilotai.llm.structured;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.common.utils.JsonExtractUtils;
import com.zy.testpilotai.llm.structured.dto.AiRequirementImpactOutputDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiRequirementImpactOutputParser {

    private final ObjectMapper objectMapper;

    /**
     * 解析新需求影响分析结果。
     */
    public AiRequirementImpactOutputDTO parse(String rawOutput) {
        if (!StringUtils.hasText(rawOutput)) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "AI 新需求影响分析输出为空，无法解析"
            );
        }

        try {
            String json = JsonExtractUtils.extractJsonObject(rawOutput);
            JsonNode root = objectMapper.readTree(json);

            /*
             * 兼容模型外层包了一层 result / data 的情况。
             */
            JsonNode body = unwrapBody(root);

            AiRequirementImpactOutputDTO output = new AiRequirementImpactOutputDTO();

            output.setChangeSummary(
                    normalizeArrayField(firstNode(body, "changeSummary", "summaryPoints", "changes"))
            );

            output.setAffectedModules(
                    normalizeArrayField(firstNode(body, "affectedModules", "modules", "impactModules"))
            );

            output.setRelatedOldRules(
                    normalizeArrayField(firstNode(body, "relatedOldRules", "oldRules", "relatedRules"))
            );

            output.setRiskPoints(
                    normalizeArrayField(firstNode(body, "riskPoints", "risks", "riskList"))
            );

            output.setRegressionScope(
                    normalizeArrayField(firstNode(body, "regressionScope", "regression", "regressionModules"))
            );

            output.setSuggestedNewTestPoints(
                    normalizeArrayField(firstNode(body, "suggestedNewTestPoints", "newTestPoints", "testPoints", "suggestions"))
            );

            output.setSummary(
                    parseSummary(firstNode(body, "summary", "conclusion", "overallSummary"))
            );

            validate(output);

            return output;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "解析 AI 新需求影响分析输出失败：" + e.getMessage()
            );
        }
    }

    /**
     * 兼容 result / data 包裹。
     */
    private JsonNode unwrapBody(JsonNode root) {
        if (root == null || root.isMissingNode() || root.isNull()) {
            return root;
        }

        if (root.path("result").isObject()) {
            return root.path("result");
        }

        if (root.path("data").isObject()) {
            return root.path("data");
        }

        return root;
    }

    /**
     * 数组类字段归一化。
     *
     * 兼容：
     * 1. 数组：原样保存。
     * 2. 对象：包装成数组。
     * 3. 字符串：包装成 [{"description":"xxx"}]。
     * 4. 空：返回 []。
     */
    private String normalizeArrayField(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "[]";
        }

        try {
            if (node.isArray()) {
                return objectMapper.writeValueAsString(node);
            }

            if (node.isObject()) {
                return objectMapper.writeValueAsString(List.of(node));
            }

            if (node.isTextual() || node.isNumber() || node.isBoolean()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("description", node.asText());
                return objectMapper.writeValueAsString(List.of(item));
            }

            return "[]";
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "影响分析数组字段归一化失败：" + e.getMessage()
            );
        }
    }

    /**
     * 解析 summary。
     */
    private String parseSummary(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return "";
        }

        if (node.isTextual() || node.isNumber() || node.isBoolean()) {
            return node.asText();
        }

        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "影响分析 summary 转换失败：" + e.getMessage()
            );
        }
    }

    private JsonNode firstNode(JsonNode root, String... fieldNames) {
        if (root == null) {
            return null;
        }

        for (String fieldName : fieldNames) {
            JsonNode value = root.path(fieldName);

            if (!value.isMissingNode() && !value.isNull()) {
                return value;
            }
        }

        return null;
    }

    /**
     * 基础兜底。
     */
    private void validate(AiRequirementImpactOutputDTO output) {
        if (!StringUtils.hasText(output.getChangeSummary())) {
            output.setChangeSummary("[]");
        }

        if (!StringUtils.hasText(output.getAffectedModules())) {
            output.setAffectedModules("[]");
        }

        if (!StringUtils.hasText(output.getRelatedOldRules())) {
            output.setRelatedOldRules("[]");
        }

        if (!StringUtils.hasText(output.getRiskPoints())) {
            output.setRiskPoints("[]");
        }

        if (!StringUtils.hasText(output.getRegressionScope())) {
            output.setRegressionScope("[]");
        }

        if (!StringUtils.hasText(output.getSuggestedNewTestPoints())) {
            output.setSuggestedNewTestPoints("[]");
        }

        if (output.getSummary() == null) {
            output.setSummary("");
        }
    }
}