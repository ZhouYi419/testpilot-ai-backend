package com.zy.testpilotai.llm.service.impl;

import com.zy.testpilotai.llm.mapper.EmbeddingCallLogMapper;
import com.zy.testpilotai.llm.model.dto.EmbeddingCallLogQueryRequest;
import com.zy.testpilotai.llm.model.entity.EmbeddingCallLog;
import com.zy.testpilotai.llm.model.vo.EmbeddingCallLogVO;
import com.zy.testpilotai.llm.service.EmbeddingCallLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingCallLogServiceImpl implements EmbeddingCallLogService {

    private final EmbeddingCallLogMapper embeddingCallLogMapper;

    @Override
    public void success(
            String bizType,
            String bizId,
            String provider,
            String modelName,
            String inputText,
            Integer embeddingDimension,
            Long durationMs
    ) {
        EmbeddingCallLog log = new EmbeddingCallLog();

        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setProvider(provider);
        log.setModelName(modelName);
        log.setStatus("SUCCESS");
        log.setInputText(inputText);
        log.setEmbeddingDimension(embeddingDimension);
        log.setDurationMs(durationMs);
        log.setCreateTime(LocalDateTime.now());

        embeddingCallLogMapper.insert(log);
    }

    @Override
    public void failed(
            String bizType,
            String bizId,
            String provider,
            String modelName,
            String inputText,
            String errorMessage,
            Long durationMs
    ) {
        EmbeddingCallLog log = new EmbeddingCallLog();

        log.setBizType(bizType);
        log.setBizId(bizId);
        log.setProvider(provider);
        log.setModelName(modelName);
        log.setStatus("FAILED");
        log.setInputText(inputText);
        log.setErrorMessage(errorMessage);
        log.setDurationMs(durationMs);
        log.setCreateTime(LocalDateTime.now());

        embeddingCallLogMapper.insert(log);
    }

    @Override
    public List<EmbeddingCallLogVO> list(EmbeddingCallLogQueryRequest request) {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EmbeddingCallLog> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<EmbeddingCallLog>()
                        .orderByDesc(EmbeddingCallLog::getCreateTime);

        if (org.springframework.util.StringUtils.hasText(request.getBizType())) {
            wrapper.eq(EmbeddingCallLog::getBizType, request.getBizType());
        }

        if (org.springframework.util.StringUtils.hasText(request.getBizId())) {
            wrapper.eq(EmbeddingCallLog::getBizId, request.getBizId());
        }

        if (org.springframework.util.StringUtils.hasText(request.getStatus())) {
            wrapper.eq(EmbeddingCallLog::getStatus, request.getStatus());
        }

        if (org.springframework.util.StringUtils.hasText(request.getProvider())) {
            wrapper.eq(EmbeddingCallLog::getProvider, request.getProvider());
        }

        return embeddingCallLogMapper.selectList(wrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    private EmbeddingCallLogVO toVO(EmbeddingCallLog log) {
        EmbeddingCallLogVO vo = new EmbeddingCallLogVO();

        vo.setId(log.getId());
        vo.setBizType(log.getBizType());
        vo.setBizId(log.getBizId());
        vo.setProvider(log.getProvider());
        vo.setModelName(log.getModelName());
        vo.setStatus(log.getStatus());
        vo.setInputText(log.getInputText());
        vo.setEmbeddingDimension(log.getEmbeddingDimension());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setDurationMs(log.getDurationMs());
        vo.setCreateTime(log.getCreateTime());

        return vo;
    }
}