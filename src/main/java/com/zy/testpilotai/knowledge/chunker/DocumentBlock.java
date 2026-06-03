package com.zy.testpilotai.knowledge.chunker;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentBlock {

    /**
     * SECTION / REQUIREMENT / TABLE / PARAGRAPH
     */
    private String blockType;

    private String title;

    private String sectionPath;

    private String moduleName;

    private String requirementId;

    private String content;

    private Integer startPosition;

    private Integer endPosition;
}