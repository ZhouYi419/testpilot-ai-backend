package com.zy.testpilotai.llm.structured;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.common.utils.JsonExtractUtils;
import com.zy.testpilotai.llm.structured.dto.AiTestCaseReviewOutputDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@RequiredArgsConstructor
public class AiTestCaseReviewOutputParser {

    private final ObjectMapper objectMapper;

    /**
     * 解析质量评审模型输出。
     */
    public AiTestCaseReviewOutputDTO parse(String rawOutput) {
        if (!StringUtils.hasText(rawOutput)) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "AI 质量评审输出为空，无法解析"
            );
        }

        try {
            String json = JsonExtractUtils.extractJsonObject(rawOutput);
            JsonNode root = objectMapper.readTree(json);

            AiTestCaseReviewOutputDTO output = new AiTestCaseReviewOutputDTO();

            output.setTotalScore(parseTotalScore(root));
            output.setDimensions(normalizeArrayField(firstNode(root, "dimensions", "scores", "scoreDimensions")));
            output.setMissingPoints(normalizeArrayField(firstNode(root, "missingPoints", "missing", "missingTestPoints")));
            output.setDuplicateCases(normalizeArrayField(firstNode(root, "duplicateCases", "duplicates")));
            output.setLowQualityCases(normalizeArrayField(firstNode(root, "lowQualityCases", "badCases", "lowQuality")));
            output.setSummary(parseSummary(firstNode(root, "summary", "comment", "conclusion")));

            validate(output);

            return output;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "解析 AI 质量评审输出失败：" + e.getMessage()
            );
        }
    }

    /**
     * 解析总分。
     */
    private Integer parseTotalScore(JsonNode root) {
        JsonNode scoreNode = firstNode(root, "totalScore", "score", "total_score");

        if (scoreNode == null || scoreNode.isMissingNode() || scoreNode.isNull()) {
            return 0;
        }

        int score;

        if (scoreNode.isNumber()) {
            score = scoreNode.asInt();
        } else {
            String scoreText = scoreNode.asText("0").replaceAll("[^0-9]", "");
            if (!StringUtils.hasText(scoreText)) {
                score = 0;
            } else {
                score = Integer.parseInt(scoreText);
            }
        }

        if (score < 0) {
            return 0;
        }

        if (score > 100) {
            return 100;
        }

        return score;
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

        return toJson(node);
    }

    /**
     * 把模型输出的数组型字段统一归一化为 JSON 数组字符串。
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
                return objectMapper.writeValueAsString(java.util.List.of(node));
            }

            if (node.isTextual() || node.isNumber() || node.isBoolean()) {
                java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
                item.put("description", node.asText());
                return objectMapper.writeValueAsString(java.util.List.of(item));
            }

            return "[]";
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "质量评审字段 JSON 归一化失败：" + e.getMessage()
            );
        }
    }

    /**
     * 按多个候选字段名查找第一个存在的字段。
     */
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

    private String toJson(JsonNode node) {
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "质量评审 summary JSON 转换失败：" + e.getMessage()
            );
        }
    }

    /**
     * 基础校验和兜底。
     */
    private void validate(AiTestCaseReviewOutputDTO output) {
        if (output.getTotalScore() == null) {
            output.setTotalScore(0);
        }

        if (!StringUtils.hasText(output.getDimensions())) {
            output.setDimensions("[]");
        }

        if (!StringUtils.hasText(output.getMissingPoints())) {
            output.setMissingPoints("[]");
        }

        if (!StringUtils.hasText(output.getDuplicateCases())) {
            output.setDuplicateCases("[]");
        }

        if (!StringUtils.hasText(output.getLowQualityCases())) {
            output.setLowQualityCases("[]");
        }

        if (output.getSummary() == null) {
            output.setSummary("");
        }
    }
}