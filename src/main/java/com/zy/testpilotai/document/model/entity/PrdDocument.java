package com.zy.testpilotai.document.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("prd_document")
public class PrdDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 归属的项目ID，关联 project 表
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
     * 文档类型/格式
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
     * 对象存储桶名称
     */
    private String bucketName;

    /**
     * 对象存储文件路径/名称
     */
    private String objectName;

    /**
     * 文件内容的哈希值
     */
    private String contentHash;

    /**
     * 文档解析状态
     */
    private String parseStatus;

    /**
     * 解析后的纯文本内容
     */
    private String rawText;

    /**
     * 解析失败时的错误信息
     */
    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}