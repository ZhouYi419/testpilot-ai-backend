package com.zy.testpilotai.agent.planner.tool.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractAgentToolExecutor {

    protected final ObjectMapper objectMapper;

    /**
     * 参数转换为指定 DTO。
     */
    protected <T> T convert(Map<String, Object> inputParams, Class<T> clazz) {
        return objectMapper.convertValue(inputParams, clazz);
    }

    /**
     * 校验必填参数。
     */
    protected void checkRequired(Map<String, Object> inputParams, List<String> requiredParams) {
        for (String param : requiredParams) {
            Object value = inputParams.get(param);

            if (value == null) {
                throw new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "工具缺少必填参数：" + param
                );
            }

            if (value instanceof String text && !StringUtils.hasText(text)) {
                throw new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "工具必填参数不能为空：" + param
                );
            }
        }
    }
}