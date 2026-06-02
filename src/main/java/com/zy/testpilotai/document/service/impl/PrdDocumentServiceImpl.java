package com.zy.testpilotai.document.service.impl;

import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.document.mapper.PrdDocumentMapper;
import com.zy.testpilotai.document.model.entity.PrdDocumentEntity;
import com.zy.testpilotai.document.model.vo.PrdDocumentVO;
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

    @Value("${testpilot.file.upload-dir:./data/uploads}")
    private String uploadDir;

    /**
     * 上传 PRD 文档并持久化元数据
     */
    @Override
    public Long uploadDocument(Long projectId, String versionName, String description, MultipartFile file) {
        // 1. 基础参数校验
        validateUploadParams(projectId, versionName, file);

        // 2. 校验关联的项目是否存在
        ProjectEntity project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "项目不存在，无法上传 PRD 文档");
        }

        // 3. 文件名安全处理与校验
        // 使用 StringUtils.cleanPath 清理路径，提取纯文件名
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        if (!StringUtils.hasText(originalFileName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不能为空");
        }

        // 防止目录穿越攻击 (Path Traversal Attack)，例如文件名伪造为 "../../../etc/passwd"
        if (originalFileName.contains("..")) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件名不合法");
        }

        // 4. 校验文件扩展名是否在白名单内
        String fileType = getFileExtension(originalFileName);
        if (!ALLOWED_FILE_TYPES.contains(fileType.toLowerCase())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "暂不支持该文件类型：" + fileType);
        }

        try {
            // 5. 构建安全的物理存储路径
            String safeVersion = versionName.trim().replaceAll("[^a-zA-Z0-9._-]", "_");
            String dateDir = LocalDate.now().toString(); // 按日期分目录存储，避免单目录文件过多导致性能瓶颈

            // 最终存储目录格式：{uploadDir}/project-{projectId}/{safeVersion}/{YYYY-MM-DD}
            Path projectUploadDir = Path.of(uploadDir, "project-" + projectId, safeVersion, dateDir);
            Files.createDirectories(projectUploadDir); // 确保目录存在，不存在则自动级联创建

            // 生成唯一标识作为存储文件名，防止同名文件互相覆盖
            String savedFileName = UUID.randomUUID() + "." + fileType;
            Path targetPath = projectUploadDir.resolve(savedFileName);

            // 6. 执行文件写入物理磁盘操作
            file.transferTo(targetPath.toFile());

            // 7. 计算文件的 SHA-256 哈希值（用于后续的文件完整性校验或秒传/去重功能）
            String contentHash = calculateSha256(targetPath);

            // 8. 组装实体类并落库
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

            // 执行插入数据库操作
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

    /**
     * 根据项目 ID 和版本号查询文档列表
     */
    @Override
    public List<PrdDocumentVO> listDocuments(Long projectId, String versionName) {
        // 1. 参数校验
        if (projectId == null || projectId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "项目ID不合法");
        }

        // 2. 校验项目是否存在
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

    /**
     * 根据 ID 获取文档详细信息
     */
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

    /**
     * 逻辑删除 PRD 文档记录
     */
    @Override
    public Boolean deleteDocument(Long documentId) {
        if (documentId == null || documentId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文档ID不合法");
        }

        PrdDocumentEntity document = prdDocumentMapper.selectById(documentId);
        if (document == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "PRD 文档不存在");
        }

        // 执行逻辑删除
        int rows = prdDocumentMapper.logicDeleteById(documentId);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "删除 PRD 文档失败");
        }

        return true;
    }

    // ================= private =================

    /**
     * 校验上传接口的核心必填参数
     */
    private void validateUploadParams(Long projectId, String versionName, MultipartFile file) {
        if (projectId == null || projectId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "项目ID不合法");
        }
        if (!StringUtils.hasText(versionName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "PRD 版本号不能为空");
        }
        // 防止数据库字段超长导致截断报错
        if (versionName.trim().length() > 100) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "PRD 版本号不能超过100个字符");
        }
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "上传文件不能为空");
        }
    }

    /**
     * 从原始文件名中提取文件扩展名（不带点）
     */
    private String getFileExtension(String fileName) {
        int index = fileName.lastIndexOf(".");
        // 如果没有找到点，或者点在字符串的最后一位（如 "filename."），则判定为缺少扩展名
        if (index < 0 || index == fileName.length() - 1) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "文件缺少扩展名");
        }
        return fileName.substring(index + 1).toLowerCase();
    }

    /**
     * 读取物理文件的流，计算其 SHA-256 哈希值
     */
    private String calculateSha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        // 使用 try-with-resources 语法，确保文件流使用完毕后自动安全关闭
        try (InputStream inputStream = Files.newInputStream(path)) {
            // 设置 8KB 的读取缓冲区，避免将整个大文件一次性加载进内存（防止 OOM）
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, length);
            }
        }

        // 获取计算出的摘要字节数组
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();

        // 将字节数组转换为可读的 16 进制字符串形式
        for (byte b : hashBytes) {
            String hex = Integer.toHexString(0xff & b);
            // 补齐前导 0
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