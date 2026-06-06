package com.zy.testpilotai.llm.embedding;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(name = "ai.embedding.provider", havingValue = "mock")
public class MockEmbeddingClient implements EmbeddingClient {

    @Value("${ai.embedding.mock.model-name:mock-hash-embedding-v1}")
    private String modelName;

    @Value("${ai.embedding.dimension:1024}")
    private int dimension;

    @Override
    public String modelName() {
        return modelName;
    }

    @Override
    public int dimension() {
        return dimension;
    }

    @Override
    public List<Float> embed(String text) {
        return embed(text, null, null);
    }

    @Override
    public List<Float> embed(String text, String bizType, String bizId) {
        if (!StringUtils.hasText(text)) {
            text = "";
        }

        List<Float> vector = new ArrayList<>(dimension);

        int counter = 0;
        while (vector.size() < dimension) {
            byte[] hash = sha256(text + "#" + counter);

            for (byte b : hash) {
                if (vector.size() >= dimension) {
                    break;
                }

                // 把 byte 映射到 -1 ~ 1
                float value = ((b & 0xff) / 255.0f) * 2.0f - 1.0f;
                vector.add(value);
            }

            counter++;
        }

        // 做 L2 归一化，方便 cosine 相似度计算
        return normalize(vector);
    }

    private byte[] sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Mock Embedding 生成 SHA-256 失败", e);
        }
    }

    private List<Float> normalize(List<Float> vector) {
        double sum = 0.0;

        for (Float value : vector) {
            sum += value * value;
        }

        double norm = Math.sqrt(sum);

        if (norm == 0.0) {
            return vector;
        }

        List<Float> normalized = new ArrayList<>(vector.size());

        for (Float value : vector) {
            normalized.add((float) (value / norm));
        }

        return normalized;
    }
}