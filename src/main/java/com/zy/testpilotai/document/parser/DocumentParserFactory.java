package com.zy.testpilotai.document.parser;

import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DocumentParserFactory {

    private final List<DocumentParser> parsers;

    public DocumentParser getParser(String filename) {
        return parsers.stream()
                .filter(parser -> parser.support(filename))
                .findFirst()
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "暂不支持该文件类型：" + filename
                ));
    }
}