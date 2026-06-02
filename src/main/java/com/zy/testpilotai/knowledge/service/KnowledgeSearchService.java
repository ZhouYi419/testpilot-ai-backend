package com.zy.testpilotai.knowledge.service;

import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeSearchResultVO;
import java.util.List;

public interface KnowledgeSearchService {

    List<KnowledgeSearchResultVO> search(KnowledgeSearchRequest request);
}