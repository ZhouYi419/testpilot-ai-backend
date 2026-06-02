package com.zy.testpilotai.knowledge.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeChatRequest;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeChatResponseVO;
import com.zy.testpilotai.knowledge.service.KnowledgeChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class KnowledgeChatController {

    private final KnowledgeChatService knowledgeChatService;

    /**
     * 基于项目 PRD 知识库问答
     */
    @PostMapping("/api/knowledge/chat")
    public BaseResponse<KnowledgeChatResponseVO> chat(@Valid @RequestBody KnowledgeChatRequest request) {
        return ResultUtils.success(
                knowledgeChatService.chat(request)
        );
    }
}