package com.zy.testpilotai.automation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.automation.mapper.AutomationCaseResultMapper;
import com.zy.testpilotai.automation.mapper.AutomationRunTaskMapper;
import com.zy.testpilotai.automation.mapper.AutomationScriptTaskMapper;
import com.zy.testpilotai.automation.model.dto.AutomationRunCancelRequest;
import com.zy.testpilotai.automation.model.dto.AutomationRunQueryRequest;
import com.zy.testpilotai.automation.model.dto.AutomationRunStartRequest;
import com.zy.testpilotai.automation.model.entity.AutomationCaseResult;
import com.zy.testpilotai.automation.model.entity.AutomationRunTask;
import com.zy.testpilotai.automation.model.entity.AutomationScriptTask;
import com.zy.testpilotai.automation.model.vo.AutomationCaseResultVO;
import com.zy.testpilotai.automation.model.vo.AutomationRunDetailVO;
import com.zy.testpilotai.automation.model.vo.AutomationRunTaskVO;
import com.zy.testpilotai.automation.service.AutomationRunService;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AutomationRunServiceImpl implements AutomationRunService {

    private final AutomationRunTaskMapper automationRunTaskMapper;

    private final AutomationCaseResultMapper automationCaseResultMapper;

    private final AutomationScriptTaskMapper automationScriptTaskMapper;

    private final AutomationRunAsyncProcessor automationRunAsyncProcessor;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AutomationRunTaskVO start(AutomationRunStartRequest request) {
        validateStartRequest(request);

        AutomationScriptTask scriptTask = getScriptTask(request.getScriptTaskId());

        if (!"SUCCESS".equals(scriptTask.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "脚本生成任务未成功，不能执行"
            );
        }

        String runTaskId = "ar_" + UUID.randomUUID().toString().replace("-", "");

        AutomationRunTask runTask = new AutomationRunTask();

        runTask.setRunTaskId(runTaskId);
        runTask.setScriptTaskId(scriptTask.getScriptTaskId());
        runTask.setProjectId(scriptTask.getProjectId());
        runTask.setVersionNo(scriptTask.getVersionNo());
        runTask.setModuleCode(scriptTask.getModuleCode());
        runTask.setEnvironmentName(defaultText(request.getEnvironmentName(), "local"));
        runTask.setExecutionMode("LOCAL_PROCESS");
        runTask.setBaseUrl(defaultText(request.getBaseUrl(), scriptTask.getBaseUrl()));
        runTask.setApiToken(request.getApiToken());
        runTask.setExtraEnv(toJson(request.getExtraEnv() == null ? Map.of() : request.getExtraEnv()));
        runTask.setTimeoutSeconds(request.getTimeoutSeconds() == null ? 600 : request.getTimeoutSeconds());
        runTask.setCancelRequested(0);
        runTask.setStatus("PENDING");
        runTask.setExitCode(null);
        runTask.setTotalCount(0);
        runTask.setPassedCount(0);
        runTask.setFailedCount(0);
        runTask.setErrorCount(0);
        runTask.setSkippedCount(0);
        runTask.setDurationMs(0L);
        runTask.setCreateTime(LocalDateTime.now());
        runTask.setUpdateTime(LocalDateTime.now());

        automationRunTaskMapper.insert(runTask);

        automationRunAsyncProcessor.execute(runTaskId);

        return toTaskVO(runTask);
    }

    @Override
    public List<AutomationRunTaskVO> list(AutomationRunQueryRequest request) {
        LambdaQueryWrapper<AutomationRunTask> wrapper =
                new LambdaQueryWrapper<AutomationRunTask>()
                        .orderByDesc(AutomationRunTask::getCreateTime)
                        .orderByDesc(AutomationRunTask::getId);

        if (request != null && StringUtils.hasText(request.getScriptTaskId())) {
            wrapper.eq(AutomationRunTask::getScriptTaskId, request.getScriptTaskId());
        }

        if (request != null && request.getProjectId() != null) {
            wrapper.eq(AutomationRunTask::getProjectId, request.getProjectId());
        }

        if (request != null && StringUtils.hasText(request.getVersionNo())) {
            wrapper.eq(AutomationRunTask::getVersionNo, request.getVersionNo());
        }

        if (request != null && StringUtils.hasText(request.getModuleCode())) {
            wrapper.eq(AutomationRunTask::getModuleCode, request.getModuleCode());
        }

        if (request != null && StringUtils.hasText(request.getEnvironmentName())) {
            wrapper.eq(AutomationRunTask::getEnvironmentName, request.getEnvironmentName());
        }

        if (request != null && StringUtils.hasText(request.getStatus())) {
            wrapper.eq(AutomationRunTask::getStatus, request.getStatus());
        }

        return automationRunTaskMapper.selectList(wrapper)
                .stream()
                .map(this::toTaskVO)
                .toList();
    }

    @Override
    public AutomationRunDetailVO detail(String runTaskId) {
        if (!StringUtils.hasText(runTaskId)) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "执行任务 ID 不能为空"
            );
        }

        AutomationRunTask task = getRunTask(runTaskId);

        List<AutomationCaseResultVO> caseResults =
                automationCaseResultMapper.selectList(
                                new LambdaQueryWrapper<AutomationCaseResult>()
                                        .eq(AutomationCaseResult::getRunTaskId, runTaskId)
                                        .orderByAsc(AutomationCaseResult::getId)
                        )
                        .stream()
                        .map(this::toCaseResultVO)
                        .toList();

        AutomationRunDetailVO detailVO = new AutomationRunDetailVO();
        detailVO.setTask(toTaskVO(task));
        detailVO.setCaseResults(caseResults);

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancel(AutomationRunCancelRequest request) {
        if (request == null || !StringUtils.hasText(request.getRunTaskId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "执行任务 ID 不能为空"
            );
        }

        AutomationRunTask task = getRunTask(request.getRunTaskId());

        if ("SUCCESS".equals(task.getStatus())
                || "FAILED".equals(task.getStatus())
                || "CANCELLED".equals(task.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "任务已结束，不能取消"
            );
        }

        task.setCancelRequested(1);
        task.setUpdateTime(LocalDateTime.now());

        automationRunTaskMapper.updateById(task);

        automationRunAsyncProcessor.cancelProcess(task.getRunTaskId());

        return true;
    }

    private void validateStartRequest(AutomationRunStartRequest request) {
        if (request == null || !StringUtils.hasText(request.getScriptTaskId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "脚本任务 ID 不能为空"
            );
        }

        if (request.getTimeoutSeconds() != null && request.getTimeoutSeconds() <= 0) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "超时时间必须大于 0"
            );
        }
    }

    private AutomationScriptTask getScriptTask(String scriptTaskId) {
        AutomationScriptTask task = automationScriptTaskMapper.selectOne(
                new LambdaQueryWrapper<AutomationScriptTask>()
                        .eq(AutomationScriptTask::getScriptTaskId, scriptTaskId)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "脚本任务不存在"
            );
        }

        return task;
    }

    private AutomationRunTask getRunTask(String runTaskId) {
        AutomationRunTask task = automationRunTaskMapper.selectOne(
                new LambdaQueryWrapper<AutomationRunTask>()
                        .eq(AutomationRunTask::getRunTaskId, runTaskId)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "自动化执行任务不存在"
            );
        }

        return task;
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String toJson(Object object) {
        try {
            if (object == null) {
                return "null";
            }

            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "JSON 序列化失败：" + e.getMessage()
            );
        }
    }

    private AutomationRunTaskVO toTaskVO(AutomationRunTask task) {
        AutomationRunTaskVO vo = new AutomationRunTaskVO();

        vo.setId(task.getId());
        vo.setRunTaskId(task.getRunTaskId());
        vo.setScriptTaskId(task.getScriptTaskId());
        vo.setProjectId(task.getProjectId());
        vo.setVersionNo(task.getVersionNo());
        vo.setModuleCode(task.getModuleCode());
        vo.setEnvironmentName(task.getEnvironmentName());
        vo.setExecutionMode(task.getExecutionMode());
        vo.setWorkDir(task.getWorkDir());
        vo.setReportFilePath(task.getReportFilePath());
        vo.setBaseUrl(task.getBaseUrl());
        vo.setExtraEnv(task.getExtraEnv());
        vo.setTimeoutSeconds(task.getTimeoutSeconds());
        vo.setCancelRequested(task.getCancelRequested());
        vo.setStatus(task.getStatus());
        vo.setExitCode(task.getExitCode());
        vo.setTotalCount(task.getTotalCount());
        vo.setPassedCount(task.getPassedCount());
        vo.setFailedCount(task.getFailedCount());
        vo.setErrorCount(task.getErrorCount());
        vo.setSkippedCount(task.getSkippedCount());
        vo.setDurationMs(task.getDurationMs());
        vo.setStdoutLog(task.getStdoutLog());
        vo.setStderrLog(task.getStderrLog());
        vo.setJunitXml(task.getJunitXml());
        vo.setErrorMessage(task.getErrorMessage());
        vo.setCreateTime(task.getCreateTime());
        vo.setUpdateTime(task.getUpdateTime());
        vo.setStartTime(task.getStartTime());
        vo.setEndTime(task.getEndTime());

        return vo;
    }

    private AutomationCaseResultVO toCaseResultVO(AutomationCaseResult result) {
        AutomationCaseResultVO vo = new AutomationCaseResultVO();

        vo.setId(result.getId());
        vo.setRunTaskId(result.getRunTaskId());
        vo.setClassName(result.getClassName());
        vo.setCaseName(result.getCaseName());
        vo.setStatus(result.getStatus());
        vo.setTimeSeconds(result.getTimeSeconds());
        vo.setMessage(result.getMessage());
        vo.setDetail(result.getDetail());
        vo.setSystemOut(result.getSystemOut());
        vo.setSystemErr(result.getSystemErr());
        vo.setCreateTime(result.getCreateTime());

        return vo;
    }
}