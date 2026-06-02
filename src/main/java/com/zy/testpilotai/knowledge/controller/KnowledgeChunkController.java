package com.zy.testpilotai.knowledge.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeChunkVO;
import com.zy.testpilotai.knowledge.service.KnowledgeChunkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class KnowledgeChunkController {

    private final KnowledgeChunkService knowledgeChunkService;

    /**
     * 对指定 PRD 文档进行切片
     */
    @PostMapping("/api/documents/{documentId}/chunks")
    public BaseResponse<Integer> chunkDocument(@PathVariable Long documentId) {
        return ResultUtils.success(
                knowledgeChunkService.chunkDocument(documentId)
        );
    }

    /**
     * 查询指定 PRD 文档的知识片段
     */
    @GetMapping("/api/documents/{documentId}/chunks")
    public BaseResponse<List<KnowledgeChunkVO>> listChunks(@PathVariable Long documentId) {
        return ResultUtils.success(
                knowledgeChunkService.listChunksByDocumentId(documentId)
        );
    }
}