package com.zy.testpilotai.document.parser;

import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.TikaCoreProperties;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 基于 Apache Tika 的 PRD 文档解析器。
 */
@Component
public class TikaDocumentParser implements DocumentParser {

    /**
     * -1 表示不限制 Tika 默认字符数量，避免长文档被截断。
     */
    private static final int WRITE_LIMIT = -1;

    @Override
    public ParsedDocument parse(Path filePath) {
        if (filePath == null || !Files.exists(filePath)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "待解析文件不存在");
        }

        String fileName = filePath.getFileName().toString();
        String fileExtension = getFileExtension(fileName);

        try {
            ParsedDocument tikaResult = parseByTika(filePath);

            if (tikaResult != null && StringUtils.hasText(tikaResult.getContent())) {
                return tikaResult;
            }

            if ("docx".equalsIgnoreCase(fileExtension)) {
                String poiContent = parseDocxByPoi(filePath);
                if (StringUtils.hasText(poiContent)) {
                    return new ParsedDocument(cleanText(poiContent), "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
                }
            }

            return tikaResult == null
                    ? new ParsedDocument("", null)
                    : tikaResult;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文档解析失败：" + e.getMessage());
        }
    }

    private ParsedDocument parseByTika(Path filePath) {
        try {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(WRITE_LIMIT);

            Metadata metadata = new Metadata();
            metadata.set(TikaCoreProperties.RESOURCE_NAME_KEY, filePath.getFileName().toString());

            ParseContext context = new ParseContext();

            try (InputStream inputStream = new BufferedInputStream(Files.newInputStream(filePath))) {
                parser.parse(inputStream, handler, metadata, context);
            }

            String rawContent = handler.toString();
            String cleanedContent = cleanText(rawContent);
            String contentType = metadata.get(Metadata.CONTENT_TYPE);

            return new ParsedDocument(cleanedContent, contentType);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "Tika 文档解析失败：" + e.getMessage());
        }
    }

    /**
     * 使用 Apache POI 兜底解析 docx：
     * 1. 读取普通段落
     * 2. 读取表格内容
     */
    private String parseDocxByPoi(Path filePath) {
        StringBuilder content = new StringBuilder();

        try (InputStream inputStream = Files.newInputStream(filePath);
             XWPFDocument document = new XWPFDocument(inputStream)) {

            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (StringUtils.hasText(text)) {
                    content.append(text).append("\n");
                }
            }

            for (XWPFTable table : document.getTables()) {
                for (XWPFTableRow row : table.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        String text = cell.getText();
                        if (StringUtils.hasText(text)) {
                            content.append(text).append("\t");
                        }
                    }
                    content.append("\n");
                }
            }

            return content.toString();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "POI 解析 docx 失败：" + e.getMessage());
        }
    }

    private String getFileExtension(String fileName) {
        if (!StringUtils.hasText(fileName)) {
            return "";
        }

        int index = fileName.lastIndexOf(".");
        if (index < 0 || index == fileName.length() - 1) {
            return "";
        }

        return fileName.substring(index + 1).toLowerCase();
    }

    /**
     * 文本清洗：
     * 1. 统一换行符
     * 2. 合并多余空格
     * 3. 去掉大量连续空行
     * 4. 去掉首尾空白
     */
    private String cleanText(String rawText) {
        if (rawText == null) {
            return "";
        }

        return rawText
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();
    }
}