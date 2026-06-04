package com.zy.testpilotai.document.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.document.model.dto.PrdUploadRequest;
import com.zy.testpilotai.document.model.vo.DocumentChunkVO;
import com.zy.testpilotai.document.model.vo.PrdDocumentVO;
import com.zy.testpilotai.document.model.vo.PrdParseResultVO;
import com.zy.testpilotai.document.service.PrdDocumentService;
import com.zy.testpilotai.document.service.PrdParseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/prd")
@RequiredArgsConstructor
public class PrdDocumentController {

    private final PrdDocumentService prdDocumentService;

    private final PrdParseService prdParseService;

    /**
     * 上传prd文件
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public BaseResponse<PrdDocumentVO> upload(@Valid PrdUploadRequest request) {
        return ResultUtils.success(prdDocumentService.upload(request));
    }

    /**
     * 获取文件列表
     */
    @GetMapping("/list")
    public BaseResponse<List<PrdDocumentVO>> list(@RequestParam Long projectId) {
        return ResultUtils.success(prdDocumentService.listByProjectId(projectId));
    }

    /**
     * 解析prd文件
     */
    @PostMapping("/{id}/parse")
    public BaseResponse<PrdParseResultVO> parse(@PathVariable Long id) {
        return ResultUtils.success(prdParseService.parse(id));
    }

    /**
     * 获取prd文件切块结果
     */
    @GetMapping("/{id}/chunks")
    public BaseResponse<List<DocumentChunkVO>> listChunks(
            @PathVariable Long id,
            @RequestParam(required = false) String chunkType
    ) {
        return ResultUtils.success(prdParseService.listChunks(id, chunkType));
    }
}