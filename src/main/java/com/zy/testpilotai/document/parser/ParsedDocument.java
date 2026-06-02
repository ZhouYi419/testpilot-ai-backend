package com.zy.testpilotai.document.parser;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParsedDocument {
    /**
     * 解析后的纯文本内容
     */
    private String content;

    /**
     * Tika 识别出的内容类型
     */
    private String contentType;
}
