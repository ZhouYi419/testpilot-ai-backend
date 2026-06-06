package com.zy.testpilotai.llm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zy.testpilotai.llm.mapper.LlmCallLogMapper;
import com.zy.testpilotai.llm.model.dto.LlmCallLogQueryRequest;
import com.zy.testpilotai.llm.model.entity.LlmCallLog;
import com.zy.testpilotai.llm.model.vo.LlmCallLogVO;
import com.zy.testpilotai.llm.service.LlmCallLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LlmCallLogServiceImpl implements LlmCallLogService {

    private final LlmCallLogMapper llmCallLogMapper;

    @Override
    public void success(
            String bizType,
            String bizId,
            String provider,
            String modelName,
            String systemPrompt,
            String userPrompt,
            String responseText,
            Long durationMs
    ) {
        LlmCallLog log = new LlmCallLog();

        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setProvider(provider);
        log.setModelName(modelName);
        log.setStatus("SUCCESS");
        log.setSystemPrompt(systemPrompt);
        log.setUserPrompt(userPrompt);
        log.setResponseText(responseText);
        log.setDurationMs(durationMs);
        log.setCreateTime(LocalDateTime.now());

        /*
         * token 字段先预留。
         * 后续如果从 Spring AI response metadata 中拿到 usage，
         * 再补 promptTokens / completionTokens / totalTokens。
         */
        log.setPromptTokens(null);
        log.setCompletionTokens(null);
        log.setTotalTokens(null);

        llmCallLogMapper.insert(log);
    }

    @Override
    public void failed(
            String bizType,
            String bizId,
            String provider,
            String modelName,
            String systemPrompt,
            String userPrompt,
            String errorMessage,
            Long durationMs
    ) {
        LlmCallLog log = new LlmCallLog();

        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setProvider(provider);
        log.setModelName(modelName);
        log.setStatus("FAILED");
        log.setSystemPrompt(systemPrompt);
        log.setUserPrompt(userPrompt);
        log.setErrorMessage(errorMessage);
        log.setDurationMs(durationMs);
        log.setCreateTime(LocalDateTime.now());

        llmCallLogMapper.insert(log);
    }

    @Override
    public List<LlmCallLogVO> list(LlmCallLogQueryRequest request) {
        LambdaQueryWrapper<LlmCallLog> wrapper = new LambdaQueryWrapper<LlmCallLog>()
                .orderByDesc(LlmCallLog::getCreateTime);

        if (org.springframework.util.StringUtils.hasText(request.getBizType())) {
            wrapper.eq(LlmCallLog::getBizType, request.getBizType());
        }

        if (org.springframework.util.StringUtils.hasText(request.getBizId())) {
            wrapper.eq(LlmCallLog::getBizId, request.getBizId());
        }

        if (org.springframework.util.StringUtils.hasText(request.getStatus())) {
            wrapper.eq(LlmCallLog::getStatus, request.getStatus());
        }

        if (org.springframework.util.StringUtils.hasText(request.getProvider())) {
            wrapper.eq(LlmCallLog::getProvider, request.getProvider());
        }

        return llmCallLogMapper.selectList(wrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    private LlmCallLogVO toVO(LlmCallLog log) {
        LlmCallLogVO vo = new LlmCallLogVO();

        vo.setId(log.getId());
        vo.setBizType(log.getBizType());
        vo.setBizId(log.getBizId());
        vo.setProvider(log.getProvider());
        vo.setModelName(log.getModelName());
        vo.setStatus(log.getStatus());
        vo.setSystemPrompt(log.getSystemPrompt());
        vo.setUserPrompt(log.getUserPrompt());
        vo.setResponseText(log.getResponseText());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setPromptTokens(log.getPromptTokens());
        vo.setCompletionTokens(log.getCompletionTokens());
        vo.setTotalTokens(log.getTotalTokens());
        vo.setDurationMs(log.getDurationMs());
        vo.setCreateTime(log.getCreateTime());

        return vo;
    }
}