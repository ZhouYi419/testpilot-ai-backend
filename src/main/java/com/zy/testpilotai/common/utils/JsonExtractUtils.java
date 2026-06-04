package com.zy.testpilotai.common.utils;

import org.springframework.util.StringUtils;

public class JsonExtractUtils {

    private JsonExtractUtils() {
    }

    /**
     * 从模型输出中提取 JSON 字符串。
     *
     * 支持以下情况：
     * 1. 纯 JSON：{"testCases":[]}
     * 2. Markdown JSON：```json ... ```
     * 3. 前后有解释文字：xxx {"testCases":[]} xxx
     *
     * @param text 模型原始输出
     * @return JSON 字符串
     */
    public static String extractJsonObject(String text) {
        if (!StringUtils.hasText(text)) {
            return "";
        }

        String cleanText = text.trim();

        // 去掉 Markdown 代码块标记
        cleanText = cleanText
                .replaceAll("^```json", "")
                .replaceAll("^```", "")
                .replaceAll("```$", "")
                .trim();

        int start = cleanText.indexOf("{");
        int end = cleanText.lastIndexOf("}");

        if (start >= 0 && end > start) {
            return cleanText.substring(start, end + 1);
        }

        return cleanText;
    }
}