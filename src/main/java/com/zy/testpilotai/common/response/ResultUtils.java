package com.zy.testpilotai.common.response;

public class ResultUtils {

    public static <T> BaseResponse<T> success(T data) {
        return new BaseResponse<>(0, "ok", data);
    }

    public static BaseResponse<Boolean> success() {
        return new BaseResponse<>(0, "ok", true);
    }

    public static <T> BaseResponse<T> error(int code, String message) {
        return new BaseResponse<>(code, message, null);
    }
}