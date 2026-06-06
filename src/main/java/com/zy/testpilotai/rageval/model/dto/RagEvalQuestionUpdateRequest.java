package com.zy.testpilotai.rageval.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class RagEvalQuestionUpdateRequest {

    /**
     * 问题业务 ID。
     */
    private String questionId;

    /**
     * 问题文本。
     */
    private String questionText;

    /**
     * 标准答案。
     */
    private String standardAnswer;

    /**
     * 期望关键词。
     */
    private List<String> expectedKeywords;

    /**
     * 期望 Chunk ID。
     */
    private List<String> expectedChunkIds;

    /**
     * 期望文档 ID。
     */
    private List<String> expectedDocumentIds;

    /**
     * 期望模块编码。
     */
    private String expectedModuleCode;

    /**
     * 期望版本号。
     */
    private String expectedVersionNo;

    /**
     * 难度。
     */
    private String difficulty;
}