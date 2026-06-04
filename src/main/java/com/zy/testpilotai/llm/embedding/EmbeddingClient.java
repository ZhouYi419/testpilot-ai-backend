package com.zy.testpilotai.llm.embedding;

import java.util.List;

public interface EmbeddingClient {

    /**
     * 返回当前 Embedding 模型名称。
     */
    String modelName();

    /**
     * 返回向量维度。
     */
    int dimension();

    /**
     * 将文本转换成向量。
     */
    List<Float> embed(String text);
}