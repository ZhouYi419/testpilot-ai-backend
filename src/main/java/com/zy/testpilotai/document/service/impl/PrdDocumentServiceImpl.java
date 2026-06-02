package com.zy.testpilotai.document.service.impl;

import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.document.mapper.PrdDocumentMapper;
import com.zy.testpilotai.document.model.entity.PrdDocumentEntity;
import com.zy.testpilotai.document.model.vo.PrdDocumentContentVO;
import com.zy.testpilotai.document.model.vo.PrdDocumentVO;
import com.zy.testpilotai.document.parser.DocumentParser;
import com.zy.testpilotai.document.parser.ParsedDocument;
import com.zy.testpilotai.document.service.PrdDocumentService;
import com.zy.testpilotai.project.mapper.ProjectMapper;
import com.zy.testpilotai.project.model.entity.ProjectEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PrdDocumentServiceImpl implements PrdDocumentService {

    private static final Set<String> ALLOWED_FILE_TYPES = Set.of(
            "pdf", "doc", "docx", "txt", "md", "markdown"
    );

    private final PrdDocumentMapper prdDocumentMapper;

    private final ProjectMapper projectMapper;

    private final DocumentParser documentParser;

    @Value("${testpilot.file.upload-dir:./data/uploads}")
    private String uploadDir;

    @Override
    public Long uploadDocument(Long projectId, String versionName, String description, MultipartFile file) {
        validateUploadParams(projectId, versionName, file);

        ProjectEntity project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "项目不存在，无法上传 PRD 文档");
        }

        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (!StringUtils.hasText(originalFileName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        if (originalFileName.contains("..")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不合法");
        }

        String fileType = getFileExtension(originalFileName);
        if (!ALLOWED_FILE_TYPES.contains(fileType.toLowerCase())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂不支持该文件类型：" + fileType);
        }

        try {
            String safeVersion = versionName.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
            String dateDir = LocalDate.now().toString();

            Path projectUploadDir = Path.of(uploadDir, "project-" + projectId, safeVersion, dateDir)
                    .toAbsolutePath()
                    .normalize();

            Files.createDirectories(projectUploadDir);

            String savedFileName = UUID.randomUUID() + "." + fileType;
            Path targetPath = projectUploadDir.resolve(savedFileName)
                    .toAbsolutePath()
                    .normalize();

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            String contentHash = calculateSha256(targetPath);

            PrdDocumentEntity document = new PrdDocumentEntity();
            document.setProjectId(projectId);
            document.setVersionName(versionName.trim());
            document.setFileName(savedFileName);
            document.setOriginalFileName(originalFileName);
            document.setFilePath(targetPath.toString());
            document.setFileType(fileType);
            document.setFileSize(file.getSize());
            document.setParseStatus("PENDING");
            document.setIndexStatus("PENDING");
            document.setParsedContent(null);
            document.setContentHash(contentHash);
            document.setDescription(description);
            document.setErrorMessage(null);

            int rows = prdDocumentMapper.insert(document);
            if (rows <= 0 || document.getId() == null) {
                throw new BusinessException(ErrorCode.DATABASE_ERROR, "PRD 文档记录保存失败");
            }

            return document.getId();
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "PRD 文档上传失败：" + e.getMessage());
        }
    }

    @Override
    public List<PrdDocumentVO> listDocuments(Long projectId, String versionName) {
        if (projectId == null || projectId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "项目ID不合法");
        }

        ProjectEntity project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "项目不存在");
        }

        String queryVersionName = StringUtils.hasText(versionName) ? versionName.trim() : null;

        return prdDocumentMapper.selectByProjectId(projectId, queryVersionName)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public PrdDocumentVO getDocumentById(Long documentId) {
        if (documentId == null || documentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文档ID不合法");
        }

        PrdDocumentEntity document = prdDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "PRD 文档不存在");
        }

        return toVO(document);
    }

    @Override
    public PrdDocumentContentVO getDocumentContent(Long documentId) {
        if (documentId == null || documentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文档ID不合法");
        }

        PrdDocumentEntity document = prdDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "PRD 文档不存在");
        }

        PrdDocumentContentVO vo = new PrdDocumentContentVO();
        vo.setId(document.getId());
        vo.setProjectId(document.getProjectId());
        vo.setVersionName(document.getVersionName());
        vo.setOriginalFileName(document.getOriginalFileName());
        vo.setParseStatus(document.getParseStatus());
        vo.setParsedContent(document.getParsedContent());
        vo.setErrorMessage(document.getErrorMessage());
        vo.setUpdatedAt(document.getUpdatedAt());

        return vo;
    }

    @Override
    public Boolean parseDocument(Long documentId) {
        if (documentId == null || documentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文档ID不合法");
        }

        PrdDocumentEntity document = prdDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "PRD 文档不存在");
        }

        Path filePath = Path.of(document.getFilePath());
        if (!Files.exists(filePath)) {
            prdDocumentMapper.updateParseStatus(documentId, "FAILED", "原始文件不存在：" + document.getFilePath());
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "原始文件不存在，无法解析");
        }

        try {
            prdDocumentMapper.updateParseStatus(documentId, "PARSING", null);

            ParsedDocument parsedDocument = documentParser.parse(filePath);
            String parsedContent = parsedDocument.getContent();

            if (!StringUtils.hasText(parsedContent)) {
                prdDocumentMapper.updateParseStatus(documentId, "FAILED", "文档解析结果为空");
                throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文档解析结果为空");
            }

            prdDocumentMapper.updateParsedContent(documentId, "PARSED", parsedContent, null);
            return true;
        } catch (BusinessException e) {
            prdDocumentMapper.updateParseStatus(documentId, "FAILED", e.getMessage());
            throw e;
        } catch (Exception e) {
            prdDocumentMapper.updateParseStatus(documentId, "FAILED", e.getMessage());
            throw new BusinessException(ErrorCode.FILE_UPLOAD_ERROR, "文档解析失败：" + e.getMessage());
        }
    }

    @Override
    public Boolean deleteDocument(Long documentId) {
        if (documentId == null || documentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文档ID不合法");
        }

        PrdDocumentEntity document = prdDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "PRD 文档不存在");
        }

        int rows = prdDocumentMapper.logicDeleteById(documentId);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "删除 PRD 文档失败");
        }

        return true;
    }

    private void validateUploadParams(Long projectId, String versionName, MultipartFile file) {
        if (projectId == null || projectId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "项目ID不合法");
        }

        if (!StringUtils.hasText(versionName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "PRD 版本号不能为空");
        }

        if (versionName.trim().length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "PRD 版本号不能超过100个字符");
        }

        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }
    }

    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        if (index < 0 || index == fileName.length() - 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件缺少扩展名");
        }
        return fileName.substring(index + 1).toLowerCase();
    }

    private String calculateSha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (InputStream inputStream = Files.newInputStream(path)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }
        }

        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();

        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append("0");
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    private PrdDocumentVO toVO(PrdDocumentEntity entity) {
        PrdDocumentVO vo = new PrdDocumentVO();
        vo.setId(entity.getId());
        vo.setProjectId(entity.getProjectId());
        vo.setVersionName(entity.getVersionName());
        vo.setFileName(entity.getFileName());
        vo.setOriginalFileName(entity.getOriginalFileName());
        vo.setFileType(entity.getFileType());
        vo.setFileSize(entity.getFileSize());
        vo.setParseStatus(entity.getParseStatus());
        vo.setIndexStatus(entity.getIndexStatus());
        vo.setDescription(entity.getDescription());
        vo.setErrorMessage(entity.getErrorMessage());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}