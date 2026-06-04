package com.zy.testpilotai.knowledge.service;

import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeBuildResultVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeBuildTaskVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeSearchResultVO;
import java.util.List;

public interface KnowledgeBaseService {

    /**
     * 构建某个文档的知识库向量。
     */
    KnowledgeBuildResultVO buildDocument(Long documentId);

    /**
     * 查询知识库构建任务详情。
     */
    KnowledgeBuildTaskVO getBuildTask(String taskId);

    /**
     * 知识库检索。
     */
    List<KnowledgeSearchResultVO> search(KnowledgeSearchRequest request);
}