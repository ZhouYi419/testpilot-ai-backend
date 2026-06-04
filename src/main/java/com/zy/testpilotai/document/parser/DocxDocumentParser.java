package com.zy.testpilotai.document.parser;

import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DocxDocumentParser implements DocumentParser {

    @Override
    public boolean support(String filename) {
        return filename != null && filename.toLowerCase().endsWith(".docx");
    }

    @Override
    public ParsedDocument parse(String filename, byte[] bytes) {
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            List<XWPFParagraph> paragraphs = document.getParagraphs();

            String text = paragraphs.stream()
                    .map(XWPFParagraph::getText)
                    .filter(line -> line != null && !line.isBlank())
                    .collect(Collectors.joining("\n"));

            return new ParsedDocument(text);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "DOCX 解析失败：" + e.getMessage());
        }
    }
}