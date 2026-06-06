package com.zy.testpilotai.llm.embedding;

import java.util.List;

public interface EmbeddingClient {

    /**
     * 当前 Embedding 模型名称。
     */
    String modelName();

    /**
     * 当前 Embedding 向量维度。
     */
    int dimension();

    /**
     * 生成文本向量。
     */
    List<Float> embed(String text);

    /**
     * 带业务上下文的向量生成。
     */
    default List<Float> embed(String text, String bizType, String bizId) {
        return embed(text);
    }
}