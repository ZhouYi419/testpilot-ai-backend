package com.zy.testpilotai.knowledge.service;

import com.zy.testpilotai.knowledge.model.dto.KnowledgeEvaluateQueryRequest;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeEvaluateRequest;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeEvaluateResultVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeEvaluateTaskVO;
import java.util.List;

public interface KnowledgeEvaluateService {

    /**
     * 执行知识库质量评估。
     */
    KnowledgeEvaluateResultVO evaluate(KnowledgeEvaluateRequest request);

    /**
     * 查询评估任务列表。
     */
    List<KnowledgeEvaluateTaskVO> list(KnowledgeEvaluateQueryRequest request);

    /**
     * 查询评估详情。
     */
    KnowledgeEvaluateResultVO detail(String evaluateTaskId);
}