package com.zy.testpilotai.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_FOUND_ERROR(40400, "数据不存在"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    FILE_UPLOAD_ERROR(50010, "文件上传失败");

    private final int code;

    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}