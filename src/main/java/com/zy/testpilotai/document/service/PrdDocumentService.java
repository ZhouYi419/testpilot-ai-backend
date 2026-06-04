package com.zy.testpilotai.document.service;

import com.zy.testpilotai.document.model.dto.PrdUploadRequest;
import com.zy.testpilotai.document.model.vo.PrdDocumentVO;
import java.util.List;

public interface PrdDocumentService {
    /**
     * 上传prd文件
     */
    PrdDocumentVO upload(PrdUploadRequest request);

    /**
     * 查询prd列表
     */
    List<PrdDocumentVO> listByProjectId(Long projectId);
}