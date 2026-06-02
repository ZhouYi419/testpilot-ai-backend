package com.zy.testpilotai.knowledge.service;

import com.zy.testpilotai.knowledge.model.vo.KnowledgeChunkVO;
import java.util.List;

public interface KnowledgeChunkService {

    Integer chunkDocument(Long documentId);

    List<KnowledgeChunkVO> listChunksByDocumentId(Long documentId);
}