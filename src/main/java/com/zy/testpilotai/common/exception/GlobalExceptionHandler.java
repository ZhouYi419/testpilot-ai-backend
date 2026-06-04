package com.zy.testpilotai.common.exception;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.warn("BusinessException: {}", e.getMessage());
        return ResultUtils.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            IllegalArgumentException.class
    })
    public BaseResponse<?> paramsExceptionHandler(Exception e) {
        log.warn("ParamsException: {}", e.getMessage());
        return ResultUtils.error(ErrorCode.PARAMS_ERROR.getCode(), "请求参数错误：" + e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public BaseResponse<?> exceptionHandler(Exception e) {
        log.error("SystemException", e);
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR.getCode(), "系统异常：" + e.getMessage());
    }
}