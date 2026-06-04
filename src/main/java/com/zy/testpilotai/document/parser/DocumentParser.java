package com.zy.testpilotai.document.parser;

public interface DocumentParser {

    /**
     * 判断当前解析器是否支持处理该文件
     */
    boolean support(String filename);

    /**
     * 执行文档解析
     */
    ParsedDocument parse(String filename, byte[] bytes);
}