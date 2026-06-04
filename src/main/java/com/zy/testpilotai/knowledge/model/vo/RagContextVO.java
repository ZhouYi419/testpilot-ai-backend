package com.zy.testpilotai.knowledge.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class RagContextVO {

    /**
     * 原始查询内容
     */
    private String query;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 版本号，可为空
     */
    private String versionNo;

    /**
     * 模块编码，可为空
     */
    private String moduleCode;

    /**
     * 拼接后的 RAG 上下文文本。
     */
    private String contextText;

    /**
     * 召回结果列表。
     */
    private List<KnowledgeSearchResultVO> references;
}