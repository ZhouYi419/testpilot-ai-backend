package com.zy.testpilotai.knowledge.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class KnowledgeChatResponseVO {

    /**
     * AI 回答
     */
    private String answer;

    /**
     * 使用的 LLM 模型
     */
    private String modelName;

    /**
     * 引用来源
     */
    private List<KnowledgeReferenceVO> references;
}