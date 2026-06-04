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
}