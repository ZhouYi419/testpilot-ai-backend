package com.zy.testpilotai.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(0, "success"),

    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_FOUND_ERROR(40400, "资源不存在"),
    FORBIDDEN_ERROR(40300, "无权限访问"),
    SYSTEM_ERROR(50000, "系统内部异常"),

    DATABASE_ERROR(50010, "数据库操作异常"),
    REDIS_ERROR(50020, "Redis 操作异常"),
    FILE_UPLOAD_ERROR(50030, "文件上传异常"),
    AI_SERVICE_ERROR(50040, "AI 服务异常"),
    KNOWLEDGE_BASE_ERROR(50050, "知识库异常");

    private final int code;

    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}