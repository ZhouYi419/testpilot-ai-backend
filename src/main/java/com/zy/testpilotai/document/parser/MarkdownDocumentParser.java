package com.zy.testpilotai.document.parser;

import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;

@Component
public class MarkdownDocumentParser implements DocumentParser {

    @Override
    public boolean support(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".md");
    }

    @Override
    public ParsedDocument parse(String filename, byte[] bytes) {
        return new ParsedDocument(new String(bytes, StandardCharsets.UTF_8));
    }
}