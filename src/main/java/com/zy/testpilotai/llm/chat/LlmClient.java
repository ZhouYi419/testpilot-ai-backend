package com.zy.testpilotai.llm.chat;

public interface LlmClient {

    /**
     * 获取当前聊天模型名称。
     */
    String modelName();

    /**
     * 调用聊天模型。
     */
    String chat(String systemPrompt, String userPrompt);

    /**
     * 带业务上下文的模型调用。
     */
    default String chat(
            String systemPrompt,
            String userPrompt,
            String bizType,
            String bizId
    ) {
        return chat(systemPrompt, userPrompt);
    }
}