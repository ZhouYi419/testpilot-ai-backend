package com.zy.testpilotai.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    PARAMS_ERROR(40000, "请求参数错误"),
    NOT_FOUND_ERROR(40400, "数据不存在"),
    SYSTEM_ERROR(50000, "系统内部异常"),
    FILE_UPLOAD_ERROR(50010, "文件上传失败"),
    AI_EMBEDDING_ERROR(50020, "Embedding 调用失败"),
    AI_CHAT_ERROR(50030, "LLM 调用失败"),
    AI_OUTPUT_PARSE_ERROR(50040, "AI 输出解析失败"),
    TESTCASE_REVIEW_ERROR(50050, "测试用例质量评审失败"),
    TESTCASE_COMPLETE_ERROR(50060, "缺失测试用例补全失败"),
    REQUIREMENT_IMPACT_ERROR(50070, "新需求影响分析失败"),
    INCREMENTAL_TESTCASE_ERROR(50080, "增量测试用例生成失败"),
    ;

    private final int code;

    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}