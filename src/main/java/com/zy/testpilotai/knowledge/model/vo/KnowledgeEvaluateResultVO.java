package com.zy.testpilotai.knowledge.model.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class KnowledgeEvaluateResultVO {

    /**
     * 评估任务。
     */
    private KnowledgeEvaluateTaskVO task;

    /**
     * 评估明细。
     */
    private List<KnowledgeEvaluateItemVO> items = new ArrayList<>();
}