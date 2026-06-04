package com.zy.testpilotai.common.utils;

import org.springframework.util.StringUtils;
import java.util.HashSet;
import java.util.Set;

public class CaseSimilarityUtils {

    private CaseSimilarityUtils() {
    }

    /**
     * 计算两个用例文本的相似度。
     */
    public static double similarity(String textA, String textB) {
        String normalizedA = normalize(textA);
        String normalizedB = normalize(textB);

        if (!StringUtils.hasText(normalizedA) || !StringUtils.hasText(normalizedB)) {
            return 0.0;
        }

        if (normalizedA.equals(normalizedB)) {
            return 1.0;
        }

        Set<String> gramsA = toCharBigrams(normalizedA);
        Set<String> gramsB = toCharBigrams(normalizedB);

        if (gramsA.isEmpty() || gramsB.isEmpty()) {
            return 0.0;
        }

        Set<String> intersection = new HashSet<>(gramsA);
        intersection.retainAll(gramsB);

        Set<String> union = new HashSet<>(gramsA);
        union.addAll(gramsB);

        return intersection.size() * 1.0 / union.size();
    }

    /**
     * 文本归一化：
     * 1. 去掉空白字符
     * 2. 转小写
     * 3. 去掉常见标点
     */
    private static String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[，。！？、,.!?;；:：\"'“”‘’\\[\\]【】()（）{}<>《》]", "")
                .trim();
    }

    /**
     * 生成字符 2-gram。
     *
     * 示例：
     * 输入：会员充值
     * 输出：会员、员充、充值
     */
    private static Set<String> toCharBigrams(String text) {
        Set<String> result = new HashSet<>();

        if (text.length() == 1) {
            result.add(text);
            return result;
        }

        for (int i = 0; i < text.length() - 1; i++) {
            result.add(text.substring(i, i + 2));
        }

        return result;
    }
}