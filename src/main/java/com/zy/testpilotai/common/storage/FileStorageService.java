package com.zy.testpilotai.common.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String upload(String objectName, MultipartFile file);
}