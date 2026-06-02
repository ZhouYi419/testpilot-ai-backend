package com.zy.testpilotai.document.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.document.model.vo.PrdDocumentContentVO;
import com.zy.testpilotai.document.model.vo.PrdDocumentVO;
import com.zy.testpilotai.document.service.PrdDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class PrdDocumentController {

    private final PrdDocumentService prdDocumentService;

    /**
     * 上传 PRD 文档
     */
    @PostMapping("/api/projects/{projectId}/documents/upload")
    public BaseResponse<Long> uploadDocument(
            @PathVariable Long projectId,
            @RequestParam("versionName") String versionName,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("file") MultipartFile file
    ) {
        return ResultUtils.success(
                prdDocumentService.uploadDocument(projectId, versionName, description, file)
        );
    }

    /**
     * 查询项目下的 PRD 文档列表
     */
    @GetMapping("/api/projects/{projectId}/documents")
    public BaseResponse<List<PrdDocumentVO>> listDocuments(
            @PathVariable Long projectId,
            @RequestParam(value = "versionName", required = false) String versionName
    ) {
        return ResultUtils.success(
                prdDocumentService.listDocuments(projectId, versionName)
        );
    }

    /**
     * 查询 PRD 文档详情
     */
    @GetMapping("/api/documents/{documentId}")
    public BaseResponse<PrdDocumentVO> getDocumentById(@PathVariable Long documentId) {
        return ResultUtils.success(
                prdDocumentService.getDocumentById(documentId)
        );
    }

    /**
     * 手动解析 PRD 文档
     */
    @PostMapping("/api/documents/{documentId}/parse")
    public BaseResponse<Boolean> parseDocument(@PathVariable Long documentId) {
        return ResultUtils.success(
                prdDocumentService.parseDocument(documentId)
        );
    }

    /**
     * 查看 PRD 解析后的文本内容
     */
    @GetMapping("/api/documents/{documentId}/content")
    public BaseResponse<PrdDocumentContentVO> getDocumentContent(@PathVariable Long documentId) {
        return ResultUtils.success(
                prdDocumentService.getDocumentContent(documentId)
        );
    }

    /**
     * 删除 PRD 文档
     */
    @DeleteMapping("/api/documents/{documentId}")
    public BaseResponse<Boolean> deleteDocument(@PathVariable Long documentId) {
        return ResultUtils.success(
                prdDocumentService.deleteDocument(documentId)
        );
    }
}