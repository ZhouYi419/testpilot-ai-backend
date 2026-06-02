package com.zy.testpilotai.document.service;

import com.zy.testpilotai.document.model.vo.PrdDocumentContentVO;
import com.zy.testpilotai.document.model.vo.PrdDocumentVO;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface PrdDocumentService {

    Long uploadDocument(Long projectId, String versionName, String description, MultipartFile file);

    List<PrdDocumentVO> listDocuments(Long projectId, String versionName);

    PrdDocumentVO getDocumentById(Long documentId);

    PrdDocumentContentVO getDocumentContent(Long documentId);

    Boolean parseDocument(Long documentId);

    Boolean deleteDocument(Long documentId);
}