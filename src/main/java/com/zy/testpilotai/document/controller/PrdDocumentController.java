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
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/prd")
@RequiredArgsConstructor
public class PrdDocumentController {

    private final PrdDocumentService prdDocumentService;

    private final PrdParseService prdParseService;

    private final HttpClient imageHttpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(8))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

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

    /**
     * 图片代理预览，避免外链 CDN Referer 限制导致前端图片无法直接展示。
     */
    @GetMapping("/image-proxy")
    public ResponseEntity<byte[]> proxyImage(@RequestParam String url) throws Exception {
        URI uri = URI.create(url);
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            return ResponseEntity.badRequest().build();
        }

        HttpRequest request = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(15))
                .header("User-Agent", "Mozilla/5.0")
                .header("Referer", "")
                .GET()
                .build();

        HttpResponse<byte[]> response = imageHttpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            return ResponseEntity.status(response.statusCode()).build();
        }

        String contentType = response.headers()
                .firstValue(HttpHeaders.CONTENT_TYPE)
                .filter(value -> value.toLowerCase().startsWith("image/"))
                .orElse(MediaType.APPLICATION_OCTET_STREAM_VALUE);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(response.body());
    }
}
