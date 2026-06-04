package com.zy.testpilotai.common.utils;

import java.util.List;
import java.util.Locale;

public class VectorUtils {

    private VectorUtils() {
    }

    /**
     *  转成 pgvector 可以识别的文本格式。
     */
    public static String toPgVectorLiteral(List<Float> vector) {
        if (vector == null || vector.isEmpty()) {
            throw new IllegalArgumentException("向量不能为空");
        }

        StringBuilder builder = new StringBuilder();
        builder.append("[");

        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            /*
             * 使用 Locale.US，避免某些系统环境下小数点变成逗号。
             * 例如 0.123 必须是英文小数点格式。
             */
            builder.append(String.format(Locale.US, "%.8f", vector.get(i)));
        }

        builder.append("]");
        return builder.toString();
    }
}