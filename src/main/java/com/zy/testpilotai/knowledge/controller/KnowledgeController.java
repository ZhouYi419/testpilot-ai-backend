package com.zy.testpilotai.knowledge.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeBuildResultVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeBuildTaskVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeSearchResultVO;
import com.zy.testpilotai.knowledge.model.vo.RagContextVO;
import com.zy.testpilotai.knowledge.service.KnowledgeBaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeBaseService knowledgeBaseService;

    /**
     * 构建指定 PRD 文档的知识库向量。
     */
    @PostMapping("/build-document/{documentId}")
    public BaseResponse<KnowledgeBuildResultVO> buildDocument(@PathVariable Long documentId) {
        return ResultUtils.success(knowledgeBaseService.buildDocument(documentId));
    }

    /**
     * 查询知识库构建任务详情。
     */
    @GetMapping("/build-task/{taskId}")
    public BaseResponse<KnowledgeBuildTaskVO> getBuildTask(@PathVariable String taskId) {
        return ResultUtils.success(knowledgeBaseService.getBuildTask(taskId));
    }

    /**
     * 通用知识库检索接口。
     */
    @PostMapping("/search")
    public BaseResponse<List<KnowledgeSearchResultVO>> search(
            @RequestBody @Valid KnowledgeSearchRequest request
    ) {
        return ResultUtils.success(knowledgeBaseService.search(request));
    }

    /**
     * 按版本检索。
     */
    @PostMapping("/search-by-version")
    public BaseResponse<List<KnowledgeSearchResultVO>> searchByVersion(
            @RequestBody @Valid KnowledgeSearchRequest request
    ) {
        return ResultUtils.success(knowledgeBaseService.search(request));
    }

    /**
     * 按模块检索。
     */
    @PostMapping("/search-by-module")
    public BaseResponse<List<KnowledgeSearchResultVO>> searchByModule(
            @RequestBody @Valid KnowledgeSearchRequest request
    ) {
        return ResultUtils.success(knowledgeBaseService.search(request));
    }

    /**
     * 按版本 + 模块检索。
     */
    @PostMapping("/search-by-version-module")
    public BaseResponse<List<KnowledgeSearchResultVO>> searchByVersionModule(
            @RequestBody @Valid KnowledgeSearchRequest request
    ) {
        return ResultUtils.success(knowledgeBaseService.search(request));
    }

    /**
     * 构建 RAG 上下文。
     */
    @PostMapping("/rag-context")
    public BaseResponse<RagContextVO> buildRagContext(
            @RequestBody @Valid KnowledgeSearchRequest request
    ) {
        return ResultUtils.success(knowledgeBaseService.buildRagContext(request));
    }
}