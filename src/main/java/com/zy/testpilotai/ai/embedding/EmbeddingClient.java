package com.zy.testpilotai.ai.embedding;

import java.util.List;

public interface EmbeddingClient {

    /**
     * 将文本转换为向量
     */
    List<Double> embed(String text);

    /**
     * 当前使用的模型名称
     */
    String getModelName();
}