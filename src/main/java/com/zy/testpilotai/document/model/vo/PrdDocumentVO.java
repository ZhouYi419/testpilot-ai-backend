package com.zy.testpilotai.document.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PrdDocumentVO {

    private Long id;

    /**
     * 归属的项目ID
     */
    private Long projectId;

    /**
     * 文档版本号
     */
    private String versionNo;

    /**
     * 文档名称
     */
    private String docName;

    /**
     * 文档类型
     */
    private String docType;

    /**
     * 关联的模块编码
     */
    private String moduleCode;

    /**
     * 文档访问/下载链接
     */
    private String fileUrl;

    /**
     * 文档解析状态
     */
    private String parseStatus;

    /**
     * 上传/创建时间
     */
    private LocalDateTime createTime;
}