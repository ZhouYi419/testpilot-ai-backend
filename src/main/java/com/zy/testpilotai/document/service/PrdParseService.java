package com.zy.testpilotai.document.service;

import com.zy.testpilotai.document.model.vo.DocumentChunkVO;
import com.zy.testpilotai.document.model.vo.PrdParseResultVO;
import java.util.List;

public interface PrdParseService {
    /**
     * 解析文档
     */
    PrdParseResultVO parse(Long documentId);

    /**
     * 获取文档切片列表
     */
    List<DocumentChunkVO> listChunks(Long documentId, String chunkType);
}