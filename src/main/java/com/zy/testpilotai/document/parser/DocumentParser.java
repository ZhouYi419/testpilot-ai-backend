package com.zy.testpilotai.document.parser;

import java.nio.file.Path;

/**
 * 文档解析器接口
 */
public interface DocumentParser {

    /**
     * 解析文档为纯文本
     */
    ParsedDocument parse(Path filePath);
}
