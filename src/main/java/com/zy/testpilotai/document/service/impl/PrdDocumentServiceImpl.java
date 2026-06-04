package com.zy.testpilotai.document.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.common.storage.FileStorageService;
import com.zy.testpilotai.common.utils.HashUtils;
import com.zy.testpilotai.document.mapper.PrdDocumentMapper;
import com.zy.testpilotai.document.model.dto.PrdUploadRequest;
import com.zy.testpilotai.document.model.entity.PrdDocument;
import com.zy.testpilotai.document.model.vo.PrdDocumentVO;
import com.zy.testpilotai.document.service.PrdDocumentService;
import com.zy.testpilotai.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrdDocumentServiceImpl implements PrdDocumentService {

    private final PrdDocumentMapper prdDocumentMapper;

    private final ProjectService projectService;

    private final FileStorageService fileStorageService;

    @Value("${storage.minio.bucket-name}")
    private String bucketName;

    @Override
    public PrdDocumentVO upload(PrdUploadRequest request) {
        projectService.getById(request.getProjectId());

        MultipartFile file = request.getFile();
        String originalFilename = file.getOriginalFilename();

        if (originalFilename == null || originalFilename.isBlank()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        validateFileType(originalFilename);

        try {
            byte[] bytes = file.getBytes();
            String contentHash = HashUtils.sha256(bytes);

            String objectName = buildObjectName(
                    request.getProjectId(),
                    request.getVersionNo(),
                    originalFilename
            );

            String fileUrl = fileStorageService.upload(objectName, file);

            PrdDocument document = new PrdDocument();
            document.setProjectId(request.getProjectId());
            document.setVersionNo(request.getVersionNo());
            document.setDocName(originalFilename);
            document.setDocType(request.getDocType() == null ? "PRD" : request.getDocType());
            document.setModuleCode(request.getModuleCode());
            document.setFileUrl(fileUrl);
            document.setBucketName(bucketName);
            document.setObjectName(objectName);
            document.setContentHash(contentHash);
            document.setParseStatus("PENDING");
            document.setCreateTime(LocalDateTime.now());
            document.setUpdateTime(LocalDateTime.now());

            prdDocumentMapper.insert(document);

            return toVO(document);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "PRD 上传失败：" + e.getMessage());
        }
    }

    @Override
    public List<PrdDocumentVO> listByProjectId(Long projectId) {
        projectService.getById(projectId);

        List<PrdDocument> documents = prdDocumentMapper.selectList(
                new LambdaQueryWrapper<PrdDocument>()
                        .eq(PrdDocument::getProjectId, projectId)
                        .orderByDesc(PrdDocument::getCreateTime)
        );

        return documents.stream().map(this::toVO).toList();
    }

    private void validateFileType(String filename) {
        String lower = filename.toLowerCase();
        boolean valid = lower.endsWith(".docx")
                || lower.endsWith(".pdf")
                || lower.endsWith(".txt")
                || lower.endsWith(".md");

        if (!valid) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂只支持 docx、pdf、txt、md 文件");
        }
    }

    private String buildObjectName(Long projectId, String versionNo, String filename) {
        return "prd/"
                + projectId
                + "/"
                + versionNo
                + "/"
                + UUID.randomUUID()
                + "_"
                + filename;
    }

    private PrdDocumentVO toVO(PrdDocument document) {
        PrdDocumentVO vo = new PrdDocumentVO();
        vo.setId(document.getId());
        vo.setProjectId(document.getProjectId());
        vo.setVersionNo(document.getVersionNo());
        vo.setDocName(document.getDocName());
        vo.setDocType(document.getDocType());
        vo.setModuleCode(document.getModuleCode());
        vo.setFileUrl(document.getFileUrl());
        vo.setParseStatus(document.getParseStatus());
        vo.setCreateTime(document.getCreateTime());
        return vo;
    }
}