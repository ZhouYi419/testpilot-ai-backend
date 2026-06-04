package com.zy.testpilotai.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    /**
     * 上传文件
     */
    String upload(String objectName, MultipartFile file);

    /**
     * 读取文件
     */
    byte[] read(String objectName);
}