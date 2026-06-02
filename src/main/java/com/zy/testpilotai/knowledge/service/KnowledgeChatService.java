package com.zy.testpilotai.knowledge.service;

import com.zy.testpilotai.knowledge.model.dto.KnowledgeChatRequest;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeChatResponseVO;

public interface KnowledgeChatService {

    KnowledgeChatResponseVO chat(KnowledgeChatRequest request);
}