package com.zy.testpilotai.document.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PrdUploadRequest {

    /**
     * 归属的项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 文档版本号
     */
    @NotBlank(message = "版本号不能为空")
    private String versionNo;

    /**
     * 文档类型
     */
    private String docType = "PRD";

    /**
     * 关联的模块编码
     */
    private String moduleCode;

    /**
     * 上传的物理文件
     */
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
}