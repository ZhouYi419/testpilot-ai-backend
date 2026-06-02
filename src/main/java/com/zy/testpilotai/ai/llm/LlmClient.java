package com.zy.testpilotai.ai.llm;

public interface LlmClient {

    /**
     * 调用对话模型
     */
    String chat(String systemPrompt, String userPrompt);

    /**
     * 当前使用的模型名称
     */
    String getModelName();
}