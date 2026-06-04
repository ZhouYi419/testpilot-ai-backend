package com.zy.testpilotai.document.parser;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParsedDocument {
    /**
     * 文本内容
     */
    private String text;
}