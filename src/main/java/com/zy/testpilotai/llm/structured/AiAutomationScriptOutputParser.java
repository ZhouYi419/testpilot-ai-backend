package com.zy.testpilotai.llm.structured;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.common.utils.JsonExtractUtils;
import com.zy.testpilotai.llm.structured.dto.AiAutomationScriptFileDTO;
import com.zy.testpilotai.llm.structured.dto.AiAutomationScriptOutputDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AiAutomationScriptOutputParser {

    private final ObjectMapper objectMapper;

    public AiAutomationScriptOutputDTO parse(String rawOutput) {
        if (!StringUtils.hasText(rawOutput)) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "AI 自动化脚本输出为空，无法解析"
            );
        }

        try {
            String json = JsonExtractUtils.extractJsonObject(rawOutput);
            JsonNode root = objectMapper.readTree(json);

            JsonNode filesNode = findFilesNode(root);

            if (filesNode == null || !filesNode.isArray()) {
                throw new BusinessException(
                        ErrorCode.AI_OUTPUT_PARSE_ERROR,
                        "AI 自动化脚本输出中缺少 files 数组"
                );
            }

            List<AiAutomationScriptFileDTO> files = new ArrayList<>();

            for (JsonNode fileNode : filesNode) {
                AiAutomationScriptFileDTO file = parseFile(fileNode);
                validateFile(file);
                files.add(file);
            }

            if (files.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.AI_OUTPUT_PARSE_ERROR,
                        "AI 自动化脚本输出 files 为空"
                );
            }

            AiAutomationScriptOutputDTO output = new AiAutomationScriptOutputDTO();
            output.setFiles(files);

            return output;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "解析 AI 自动化脚本输出失败：" + e.getMessage()
            );
        }
    }

    private JsonNode findFilesNode(JsonNode root) {
        if (root == null || root.isNull() || root.isMissingNode()) {
            return null;
        }

        if (root.path("files").isArray()) {
            return root.path("files");
        }

        if (root.path("scriptFiles").isArray()) {
            return root.path("scriptFiles");
        }

        if (root.path("data").isArray()) {
            return root.path("data");
        }

        if (root.path("result").path("files").isArray()) {
            return root.path("result").path("files");
        }

        return null;
    }

    private AiAutomationScriptFileDTO parseFile(JsonNode node) {
        AiAutomationScriptFileDTO file = new AiAutomationScriptFileDTO();

        file.setFilePath(firstText(node, "filePath", "path", "name"));
        file.setFileType(firstText(node, "fileType", "type"));
        file.setDescription(firstText(node, "description", "desc"));
        file.setContent(firstText(node, "content", "fileContent", "code"));

        return file;
    }

    private void validateFile(AiAutomationScriptFileDTO file) {
        if (!StringUtils.hasText(file.getFilePath())) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "自动化脚本文件路径不能为空"
            );
        }

        if (!StringUtils.hasText(file.getContent())) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "自动化脚本文件内容不能为空，filePath=" + file.getFilePath()
            );
        }

        file.setFilePath(normalizeFilePath(file.getFilePath()));

        if (!StringUtils.hasText(file.getFileType())) {
            file.setFileType(guessFileType(file.getFilePath()));
        }

        if (!StringUtils.hasText(file.getDescription())) {
            file.setDescription("");
        }
    }

    private String normalizeFilePath(String filePath) {
        String path = filePath.trim().replace("\\", "/");

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.contains("..")) {
            throw new BusinessException(
                    ErrorCode.AI_OUTPUT_PARSE_ERROR,
                    "自动化脚本文件路径非法：" + filePath
            );
        }

        return path;
    }

    private String guessFileType(String filePath) {
        String lower = filePath.toLowerCase();

        if (lower.endsWith(".py")) {
            return "PYTHON";
        }

        if (lower.endsWith(".md")) {
            return "MARKDOWN";
        }

        if (lower.endsWith(".ini")
                || lower.endsWith(".yml")
                || lower.endsWith(".yaml")
                || lower.endsWith(".toml")) {
            return "CONFIG";
        }

        return "TEXT";
    }

    private String firstText(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            JsonNode value = node.path(fieldName);

            if (!value.isMissingNode() && !value.isNull()) {
                if (value.isTextual() || value.isNumber() || value.isBoolean()) {
                    return value.asText();
                }

                try {
                    return objectMapper.writeValueAsString(value);
                } catch (Exception e) {
                    return value.toString();
                }
            }
        }

        return null;
    }
}