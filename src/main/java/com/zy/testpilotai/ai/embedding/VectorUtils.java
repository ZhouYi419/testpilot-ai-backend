package com.zy.testpilotai.ai.embedding;

import java.util.List;
import java.util.stream.Collectors;

public class VectorUtils {

    private VectorUtils() {
    }

    /**
     * 转换成 pgvector 可识别的字符串格式：
     * [0.1,0.2,0.3]
     */
    public static String toPgVectorString(List<Double> vector) {
        if (vector == null || vector.isEmpty()) {
            return null;
        }

        return vector.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",", "[", "]"));
    }
}