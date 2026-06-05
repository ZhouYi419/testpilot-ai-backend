package com.zy.testpilotai.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.agent.mapper.AgentExecutionLogMapper;
import com.zy.testpilotai.agent.model.entity.AgentExecutionLog;
import com.zy.testpilotai.agent.model.vo.AgentExecutionLogVO;
import com.zy.testpilotai.agent.service.AgentExecutionLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AgentExecutionLogServiceImpl implements AgentExecutionLogService {

    private final AgentExecutionLogMapper agentExecutionLogMapper;

    private final ObjectMapper objectMapper;

    @Override
    public void info(
            String agentTaskId,
            Integer stepIndex,
            String stepType,
            String stepName,
            String eventType,
            String message,
            Object input,
            Object output,
            Long durationMs
    ) {
        saveLog(
                agentTaskId,
                stepIndex,
                stepType,
                stepName,
                "INFO",
                eventType,
                message,
                input,
                output,
                null,
                durationMs
        );
    }

    @Override
    public void warn(
            String agentTaskId,
            Integer stepIndex,
            String stepType,
            String stepName,
            String eventType,
            String message,
            Object input,
            Object output,
            Long durationMs
    ) {
        saveLog(
                agentTaskId,
                stepIndex,
                stepType,
                stepName,
                "WARN",
                eventType,
                message,
                input,
                output,
                null,
                durationMs
        );
    }

    @Override
    public void error(
            String agentTaskId,
            Integer stepIndex,
            String stepType,
            String stepName,
            String eventType,
            String message,
            Object input,
            Throwable throwable,
            Long durationMs
    ) {
        saveLog(
                agentTaskId,
                stepIndex,
                stepType,
                stepName,
                "ERROR",
                eventType,
                message,
                input,
                null,
                throwable,
                durationMs
        );
    }

    @Override
    public List<AgentExecutionLogVO> listByAgentTaskId(String agentTaskId) {
        return agentExecutionLogMapper.selectList(
                        new LambdaQueryWrapper<AgentExecutionLog>()
                                .eq(AgentExecutionLog::getAgentTaskId, agentTaskId)
                                .orderByAsc(AgentExecutionLog::getId)
                )
                .stream()
                .map(this::toVO)
                .toList();
    }

    private void saveLog(
            String agentTaskId,
            Integer stepIndex,
            String stepType,
            String stepName,
            String logLevel,
            String eventType,
            String message,
            Object input,
            Object output,
            Throwable throwable,
            Long durationMs
    ) {
        AgentExecutionLog log = new AgentExecutionLog();

        log.setAgentTaskId(agentTaskId);
        log.setStepIndex(stepIndex);
        log.setStepType(stepType);
        log.setStepName(stepName);
        log.setLogLevel(logLevel);
        log.setEventType(eventType);
        log.setMessage(message);
        log.setInputSnapshot(toJsonOrNull(input));
        log.setOutputSnapshot(toJsonOrNull(output));
        log.setDurationMs(durationMs);
        log.setCreateTime(LocalDateTime.now());

        if (throwable != null) {
            log.setErrorMessage(throwable.getMessage());
            log.setErrorStack(toStackTrace(throwable));
        }

        log.setModelProvider(null);
        log.setModelName(null);
        log.setPromptTokens(null);
        log.setCompletionTokens(null);
        log.setTotalTokens(null);

        agentExecutionLogMapper.insert(log);
    }

    private String toJsonOrNull(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            return "{\"serializeError\":\"" + e.getMessage() + "\"}";
        }
    }

    private String toStackTrace(Throwable throwable) {
        try {
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            throwable.printStackTrace(printWriter);
            return stringWriter.toString();
        } catch (Exception e) {
            return throwable.getMessage();
        }
    }

    private AgentExecutionLogVO toVO(AgentExecutionLog log) {
        AgentExecutionLogVO vo = new AgentExecutionLogVO();

        vo.setId(log.getId());
        vo.setAgentTaskId(log.getAgentTaskId());
        vo.setStepIndex(log.getStepIndex());
        vo.setStepType(log.getStepType());
        vo.setStepName(log.getStepName());
        vo.setLogLevel(log.getLogLevel());
        vo.setEventType(log.getEventType());
        vo.setMessage(log.getMessage());
        vo.setInputSnapshot(log.getInputSnapshot());
        vo.setOutputSnapshot(log.getOutputSnapshot());
        vo.setErrorMessage(log.getErrorMessage());
        vo.setErrorStack(log.getErrorStack());
        vo.setModelProvider(log.getModelProvider());
        vo.setModelName(log.getModelName());
        vo.setPromptTokens(log.getPromptTokens());
        vo.setCompletionTokens(log.getCompletionTokens());
        vo.setTotalTokens(log.getTotalTokens());
        vo.setDurationMs(log.getDurationMs());
        vo.setCreateTime(log.getCreateTime());

        return vo;
    }
}