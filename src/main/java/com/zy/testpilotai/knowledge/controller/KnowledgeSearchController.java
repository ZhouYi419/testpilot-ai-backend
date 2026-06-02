package com.zy.testpilotai.knowledge.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeSearchResultVO;
import com.zy.testpilotai.knowledge.service.KnowledgeSearchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class KnowledgeSearchController {

    private final KnowledgeSearchService knowledgeSearchService;

    /**
     * 向量相似度检索
     */
    @PostMapping("/api/knowledge/search")
    public BaseResponse<List<KnowledgeSearchResultVO>> search(@Valid @RequestBody KnowledgeSearchRequest request) {
        return ResultUtils.success(
                knowledgeSearchService.search(request)
        );
    }
}