package com.zy.testpilotai.knowledge.service;

import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeAnswerVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeBuildResultVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeBuildTaskVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeSearchResultVO;
import com.zy.testpilotai.knowledge.model.vo.RagContextVO;
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

    /**
     * 构建 RAG 上下文。
     */
    RagContextVO buildRagContext(KnowledgeSearchRequest request);

    /**
     * 基于知识库召回内容生成问答结果。
     */
    KnowledgeAnswerVO answer(KnowledgeSearchRequest request);
}
