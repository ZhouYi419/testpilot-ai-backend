package com.zy.testpilotai.testcase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.testcase.mapper.TestCaseMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSetCompareResultMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSetCompareTaskMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSetItemMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSetMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseVersionHistoryMapper;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetCompareQueryRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetCompareRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetSnapshotRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseVersionHistoryQueryRequest;
import com.zy.testpilotai.testcase.model.entity.TestCase;
import com.zy.testpilotai.testcase.model.entity.TestCaseSet;
import com.zy.testpilotai.testcase.model.entity.TestCaseSetCompareResult;
import com.zy.testpilotai.testcase.model.entity.TestCaseSetCompareTask;
import com.zy.testpilotai.testcase.model.entity.TestCaseSetItem;
import com.zy.testpilotai.testcase.model.entity.TestCaseVersionHistory;
import com.zy.testpilotai.testcase.model.vo.TestCaseSetCompareDetailVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseSetCompareResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseSetCompareTaskVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseVersionHistoryVO;
import com.zy.testpilotai.testcase.service.TestCaseVersionCompareService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestCaseVersionCompareServiceImpl implements TestCaseVersionCompareService {

    private final TestCaseSetMapper testCaseSetMapper;

    private final TestCaseSetItemMapper testCaseSetItemMapper;

    private final TestCaseMapper testCaseMapper;

    private final TestCaseVersionHistoryMapper versionHistoryMapper;

    private final TestCaseSetCompareTaskMapper compareTaskMapper;

    private final TestCaseSetCompareResultMapper compareResultMapper;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TestCaseVersionHistoryVO> snapshot(TestCaseSetSnapshotRequest request) {
        if (request == null || !StringUtils.hasText(request.getCaseSetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "用例集 ID 不能为空"
            );
        }

        TestCaseSet caseSet = getActiveCaseSet(request.getCaseSetId());

        String snapshotType = StringUtils.hasText(request.getSnapshotType())
                ? request.getSnapshotType()
                : "MANUAL_SNAPSHOT";

        List<TestCaseVersionHistory> histories = snapshotCaseSet(
                caseSet,
                null,
                snapshotType
        );

        return histories.stream()
                .map(this::toHistoryVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseSetCompareDetailVO compare(TestCaseSetCompareRequest request) {
        if (request == null || !StringUtils.hasText(request.getSourceCaseSetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "源用例集 ID 不能为空"
            );
        }

        if (!StringUtils.hasText(request.getTargetCaseSetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "目标用例集 ID 不能为空"
            );
        }

        if (request.getSourceCaseSetId().equals(request.getTargetCaseSetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "源用例集和目标用例集不能相同"
            );
        }

        TestCaseSet sourceSet = getActiveCaseSet(request.getSourceCaseSetId());
        TestCaseSet targetSet = getActiveCaseSet(request.getTargetCaseSetId());

        String compareTaskId = "cmp_" + UUID.randomUUID().toString().replace("-", "");

        TestCaseSetCompareTask task = createCompareTask(compareTaskId, sourceSet, targetSet);

        try {
            boolean shouldSnapshot = request.getSnapshot() == null || request.getSnapshot();

            if (shouldSnapshot) {
                snapshotCaseSet(sourceSet, compareTaskId, "SOURCE_SNAPSHOT");
                snapshotCaseSet(targetSet, compareTaskId, "TARGET_SNAPSHOT");
            }

            List<CaseSetCase> sourceCases = loadCaseSetCases(sourceSet.getCaseSetId());
            List<CaseSetCase> targetCases = loadCaseSetCases(targetSet.getCaseSetId());

            Map<String, CaseSetCase> sourceMap = toCaseMap(sourceCases);
            Map<String, CaseSetCase> targetMap = toCaseMap(targetCases);

            int addedCount = 0;
            int removedCount = 0;
            int modifiedCount = 0;
            int unchangedCount = 0;

            for (Map.Entry<String, CaseSetCase> targetEntry : targetMap.entrySet()) {
                String key = targetEntry.getKey();
                CaseSetCase targetCase = targetEntry.getValue();
                CaseSetCase sourceCase = sourceMap.get(key);

                if (sourceCase == null) {
                    insertCompareResult(
                            compareTaskId,
                            "ADDED",
                            null,
                            targetCase.testCase,
                            "目标用例集中新增该用例",
                            Map.of()
                    );
                    addedCount++;
                    continue;
                }

                Map<String, Object> fieldDiffs = buildFieldDiffs(
                        sourceCase.testCase,
                        targetCase.testCase
                );

                if (fieldDiffs.isEmpty()) {
                    insertCompareResult(
                            compareTaskId,
                            "UNCHANGED",
                            sourceCase.testCase,
                            targetCase.testCase,
                            "用例未变化",
                            Map.of()
                    );
                    unchangedCount++;
                } else {
                    insertCompareResult(
                            compareTaskId,
                            "MODIFIED",
                            sourceCase.testCase,
                            targetCase.testCase,
                            buildChangeSummary(fieldDiffs),
                            fieldDiffs
                    );
                    modifiedCount++;
                }
            }

            for (Map.Entry<String, CaseSetCase> sourceEntry : sourceMap.entrySet()) {
                String key = sourceEntry.getKey();

                if (!targetMap.containsKey(key)) {
                    CaseSetCase sourceCase = sourceEntry.getValue();

                    insertCompareResult(
                            compareTaskId,
                            "REMOVED",
                            sourceCase.testCase,
                            null,
                            "目标用例集中已删除该用例",
                            Map.of()
                    );
                    removedCount++;
                }
            }

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("sourceCaseSetId", sourceSet.getCaseSetId());
            summary.put("sourceSetName", sourceSet.getSetName());
            summary.put("sourceVersionNo", sourceSet.getVersionNo());
            summary.put("targetCaseSetId", targetSet.getCaseSetId());
            summary.put("targetSetName", targetSet.getSetName());
            summary.put("targetVersionNo", targetSet.getVersionNo());
            summary.put("addedCount", addedCount);
            summary.put("removedCount", removedCount);
            summary.put("modifiedCount", modifiedCount);
            summary.put("unchangedCount", unchangedCount);

            task.setStatus("SUCCESS");
            task.setAddedCount(addedCount);
            task.setRemovedCount(removedCount);
            task.setModifiedCount(modifiedCount);
            task.setUnchangedCount(unchangedCount);
            task.setSummary(toJson(summary));
            task.setUpdateTime(LocalDateTime.now());

            compareTaskMapper.updateById(task);

            return compareDetail(compareTaskId);
        } catch (BusinessException e) {
            markCompareFailed(task, e.getMessage());
            throw e;
        } catch (Exception e) {
            markCompareFailed(task, e.getMessage());
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "用例集对比失败：" + e.getMessage()
            );
        }
    }

    @Override
    public List<TestCaseSetCompareTaskVO> listCompareTasks(TestCaseSetCompareQueryRequest request) {
        LambdaQueryWrapper<TestCaseSetCompareTask> wrapper =
                new LambdaQueryWrapper<TestCaseSetCompareTask>()
                        .orderByDesc(TestCaseSetCompareTask::getCreateTime)
                        .orderByDesc(TestCaseSetCompareTask::getId);

        if (request != null && request.getProjectId() != null) {
            wrapper.eq(TestCaseSetCompareTask::getProjectId, request.getProjectId());
        }

        if (request != null && StringUtils.hasText(request.getSourceCaseSetId())) {
            wrapper.eq(TestCaseSetCompareTask::getSourceCaseSetId, request.getSourceCaseSetId());
        }

        if (request != null && StringUtils.hasText(request.getTargetCaseSetId())) {
            wrapper.eq(TestCaseSetCompareTask::getTargetCaseSetId, request.getTargetCaseSetId());
        }

        if (request != null && StringUtils.hasText(request.getStatus())) {
            wrapper.eq(TestCaseSetCompareTask::getStatus, request.getStatus());
        }

        return compareTaskMapper.selectList(wrapper)
                .stream()
                .map(this::toCompareTaskVO)
                .toList();
    }

    @Override
    public TestCaseSetCompareDetailVO compareDetail(String compareTaskId) {
        if (!StringUtils.hasText(compareTaskId)) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "对比任务 ID 不能为空"
            );
        }

        TestCaseSetCompareTask task = compareTaskMapper.selectOne(
                new LambdaQueryWrapper<TestCaseSetCompareTask>()
                        .eq(TestCaseSetCompareTask::getCompareTaskId, compareTaskId)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "用例集对比任务不存在"
            );
        }

        List<TestCaseSetCompareResult> results = compareResultMapper.selectList(
                new LambdaQueryWrapper<TestCaseSetCompareResult>()
                        .eq(TestCaseSetCompareResult::getCompareTaskId, compareTaskId)
                        .orderByAsc(TestCaseSetCompareResult::getId)
        );

        TestCaseSetCompareDetailVO detailVO = new TestCaseSetCompareDetailVO();
        detailVO.setTask(toCompareTaskVO(task));

        for (TestCaseSetCompareResult result : results) {
            TestCaseSetCompareResultVO vo = toCompareResultVO(result);

            switch (result.getResultType()) {
                case "ADDED" -> detailVO.getAdded().add(vo);
                case "REMOVED" -> detailVO.getRemoved().add(vo);
                case "MODIFIED" -> detailVO.getModified().add(vo);
                case "UNCHANGED" -> detailVO.getUnchanged().add(vo);
                default -> {
                }
            }
        }

        return detailVO;
    }

    @Override
    public List<TestCaseVersionHistoryVO> listHistory(TestCaseVersionHistoryQueryRequest request) {
        LambdaQueryWrapper<TestCaseVersionHistory> wrapper =
                new LambdaQueryWrapper<TestCaseVersionHistory>()
                        .orderByDesc(TestCaseVersionHistory::getCreateTime)
                        .orderByDesc(TestCaseVersionHistory::getId);

        if (request != null && request.getProjectId() != null) {
            wrapper.eq(TestCaseVersionHistory::getProjectId, request.getProjectId());
        }

        if (request != null && StringUtils.hasText(request.getVersionNo())) {
            wrapper.eq(TestCaseVersionHistory::getVersionNo, request.getVersionNo());
        }

        if (request != null && StringUtils.hasText(request.getModuleCode())) {
            wrapper.eq(TestCaseVersionHistory::getModuleCode, request.getModuleCode());
        }

        if (request != null && StringUtils.hasText(request.getCaseSetId())) {
            wrapper.eq(TestCaseVersionHistory::getCaseSetId, request.getCaseSetId());
        }

        if (request != null && request.getTestCaseId() != null) {
            wrapper.eq(TestCaseVersionHistory::getTestCaseId, request.getTestCaseId());
        }

        if (request != null && StringUtils.hasText(request.getCompareTaskId())) {
            wrapper.eq(TestCaseVersionHistory::getCompareTaskId, request.getCompareTaskId());
        }

        if (request != null && StringUtils.hasText(request.getSnapshotType())) {
            wrapper.eq(TestCaseVersionHistory::getSnapshotType, request.getSnapshotType());
        }

        return versionHistoryMapper.selectList(wrapper)
                .stream()
                .map(this::toHistoryVO)
                .toList();
    }

    private TestCaseSetCompareTask createCompareTask(
            String compareTaskId,
            TestCaseSet sourceSet,
            TestCaseSet targetSet
    ) {
        TestCaseSetCompareTask task = new TestCaseSetCompareTask();

        task.setCompareTaskId(compareTaskId);
        task.setProjectId(targetSet.getProjectId());
        task.setSourceCaseSetId(sourceSet.getCaseSetId());
        task.setTargetCaseSetId(targetSet.getCaseSetId());
        task.setStatus("RUNNING");
        task.setAddedCount(0);
        task.setRemovedCount(0);
        task.setModifiedCount(0);
        task.setUnchangedCount(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        compareTaskMapper.insert(task);

        return task;
    }

    private TestCaseSet getActiveCaseSet(String caseSetId) {
        TestCaseSet caseSet = testCaseSetMapper.selectOne(
                new LambdaQueryWrapper<TestCaseSet>()
                        .eq(TestCaseSet::getCaseSetId, caseSetId)
                        .last("LIMIT 1")
        );

        if (caseSet == null || "DELETED".equals(caseSet.getStatus())) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "用例集不存在或已删除，caseSetId=" + caseSetId
            );
        }

        return caseSet;
    }

    private List<TestCaseVersionHistory> snapshotCaseSet(
            TestCaseSet caseSet,
            String compareTaskId,
            String snapshotType
    ) {
        List<TestCaseSetItem> items = testCaseSetItemMapper.selectList(
                new LambdaQueryWrapper<TestCaseSetItem>()
                        .eq(TestCaseSetItem::getCaseSetId, caseSet.getCaseSetId())
                        .orderByAsc(TestCaseSetItem::getItemOrder)
                        .orderByAsc(TestCaseSetItem::getId)
        );

        List<TestCaseVersionHistory> histories = new ArrayList<>();

        for (TestCaseSetItem item : items) {
            TestCase testCase = testCaseMapper.selectById(item.getTestCaseId());

            if (testCase == null) {
                continue;
            }

            TestCaseVersionHistory history = new TestCaseVersionHistory();

            history.setHistoryId("tch_" + UUID.randomUUID().toString().replace("-", ""));
            history.setCompareTaskId(compareTaskId);
            history.setSnapshotType(snapshotType);
            history.setCaseSetId(caseSet.getCaseSetId());
            history.setTestCaseId(testCase.getId());
            history.setProjectId(testCase.getProjectId());
            history.setVersionNo(testCase.getVersionNo());
            history.setModuleCode(testCase.getModuleCode());
            history.setCaseTitle(testCase.getCaseTitle());
            history.setCaseType(testCase.getCaseType());
            history.setPriority(testCase.getPriority());
            history.setPrecondition(testCase.getPrecondition());
            history.setSteps(testCase.getSteps());
            history.setExpectedResult(testCase.getExpectedResult());
            history.setTestData(testCase.getTestData());
            history.setSourceReferences(testCase.getSourceReferences());
            history.setRiskPoint(testCase.getRiskPoint());
            history.setAutomationSuggestion(testCase.getAutomationSuggestion());
            history.setSourceType(testCase.getSourceType());
            history.setReviewStatus(testCase.getReviewStatus());
            history.setContentHash(hashCase(testCase));
            history.setCreateTime(LocalDateTime.now());

            versionHistoryMapper.insert(history);
            histories.add(history);
        }

        return histories;
    }

    private List<CaseSetCase> loadCaseSetCases(String caseSetId) {
        List<TestCaseSetItem> items = testCaseSetItemMapper.selectList(
                new LambdaQueryWrapper<TestCaseSetItem>()
                        .eq(TestCaseSetItem::getCaseSetId, caseSetId)
                        .orderByAsc(TestCaseSetItem::getItemOrder)
                        .orderByAsc(TestCaseSetItem::getId)
        );

        List<CaseSetCase> result = new ArrayList<>();

        for (TestCaseSetItem item : items) {
            TestCase testCase = testCaseMapper.selectById(item.getTestCaseId());

            if (testCase == null) {
                continue;
            }

            CaseSetCase caseSetCase = new CaseSetCase();
            caseSetCase.item = item;
            caseSetCase.testCase = testCase;
            caseSetCase.compareKey = buildCompareKey(testCase);

            result.add(caseSetCase);
        }

        return result;
    }

    private Map<String, CaseSetCase> toCaseMap(List<CaseSetCase> cases) {
        Map<String, CaseSetCase> map = new LinkedHashMap<>();

        for (CaseSetCase item : cases) {
            /*
             * 如果同一个用例集里出现相同标题和模块的用例，只保留第一条。
             * 后续可以升级为更复杂的重复匹配策略。
             */
            map.putIfAbsent(item.compareKey, item);
        }

        return map;
    }

    private String buildCompareKey(TestCase testCase) {
        return normalize(testCase.getModuleCode())
                + "::"
                + normalize(testCase.getCaseTitle());
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value
                .trim()
                .toLowerCase()
                .replaceAll("\\s+", "");
    }

    private Map<String, Object> buildFieldDiffs(TestCase source, TestCase target) {
        Map<String, Object> diffs = new LinkedHashMap<>();

        putDiff(diffs, "caseType", source.getCaseType(), target.getCaseType());
        putDiff(diffs, "priority", source.getPriority(), target.getPriority());
        putDiff(diffs, "precondition", source.getPrecondition(), target.getPrecondition());
        putDiff(diffs, "steps", source.getSteps(), target.getSteps());
        putDiff(diffs, "expectedResult", source.getExpectedResult(), target.getExpectedResult());
        putDiff(diffs, "testData", source.getTestData(), target.getTestData());
        putDiff(diffs, "sourceReferences", source.getSourceReferences(), target.getSourceReferences());
        putDiff(diffs, "riskPoint", source.getRiskPoint(), target.getRiskPoint());
        putDiff(diffs, "automationSuggestion", source.getAutomationSuggestion(), target.getAutomationSuggestion());

        return diffs;
    }

    private void putDiff(
            Map<String, Object> diffs,
            String fieldName,
            String oldValue,
            String newValue
    ) {
        String oldText = oldValue == null ? "" : oldValue;
        String newText = newValue == null ? "" : newValue;

        if (oldText.equals(newText)) {
            return;
        }

        Map<String, Object> diff = new HashMap<>();
        diff.put("old", oldValue);
        diff.put("new", newValue);

        diffs.put(fieldName, diff);
    }

    private String buildChangeSummary(Map<String, Object> fieldDiffs) {
        if (fieldDiffs.isEmpty()) {
            return "用例未变化";
        }

        return "字段发生变化：" + String.join("、", fieldDiffs.keySet());
    }

    private void insertCompareResult(
            String compareTaskId,
            String resultType,
            TestCase source,
            TestCase target,
            String changeSummary,
            Map<String, Object> fieldDiffs
    ) {
        TestCaseSetCompareResult result = new TestCaseSetCompareResult();

        result.setCompareTaskId(compareTaskId);
        result.setResultType(resultType);
        result.setSourceTestCaseId(source == null ? null : source.getId());
        result.setTargetTestCaseId(target == null ? null : target.getId());
        result.setSourceCaseTitle(source == null ? null : source.getCaseTitle());
        result.setTargetCaseTitle(target == null ? null : target.getCaseTitle());
        result.setChangeSummary(changeSummary);
        result.setFieldDiffs(toJson(fieldDiffs));
        result.setCreateTime(LocalDateTime.now());

        compareResultMapper.insert(result);
    }

    private void markCompareFailed(TestCaseSetCompareTask task, String errorMessage) {
        task.setStatus("FAILED");
        task.setErrorMessage(errorMessage);
        task.setUpdateTime(LocalDateTime.now());

        compareTaskMapper.updateById(task);
    }

    private String hashCase(TestCase testCase) {
        String raw = String.join(
                "|",
                nullToEmpty(testCase.getModuleCode()),
                nullToEmpty(testCase.getCaseTitle()),
                nullToEmpty(testCase.getCaseType()),
                nullToEmpty(testCase.getPriority()),
                nullToEmpty(testCase.getPrecondition()),
                nullToEmpty(testCase.getSteps()),
                nullToEmpty(testCase.getExpectedResult()),
                nullToEmpty(testCase.getTestData()),
                nullToEmpty(testCase.getSourceReferences()),
                nullToEmpty(testCase.getRiskPoint()),
                nullToEmpty(testCase.getAutomationSuggestion())
        );

        return sha256(raw);
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder builder = new StringBuilder();

            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }

            return builder.toString();
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "生成用例内容哈希失败：" + e.getMessage()
            );
        }
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
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

    private TestCaseSetCompareTaskVO toCompareTaskVO(TestCaseSetCompareTask task) {
        TestCaseSetCompareTaskVO vo = new TestCaseSetCompareTaskVO();

        vo.setId(task.getId());
        vo.setCompareTaskId(task.getCompareTaskId());
        vo.setProjectId(task.getProjectId());
        vo.setSourceCaseSetId(task.getSourceCaseSetId());
        vo.setTargetCaseSetId(task.getTargetCaseSetId());
        vo.setStatus(task.getStatus());
        vo.setAddedCount(task.getAddedCount());
        vo.setRemovedCount(task.getRemovedCount());
        vo.setModifiedCount(task.getModifiedCount());
        vo.setUnchangedCount(task.getUnchangedCount());
        vo.setSummary(task.getSummary());
        vo.setErrorMessage(task.getErrorMessage());
        vo.setCreateTime(task.getCreateTime());
        vo.setUpdateTime(task.getUpdateTime());

        return vo;
    }

    private TestCaseSetCompareResultVO toCompareResultVO(TestCaseSetCompareResult result) {
        TestCaseSetCompareResultVO vo = new TestCaseSetCompareResultVO();

        vo.setId(result.getId());
        vo.setCompareTaskId(result.getCompareTaskId());
        vo.setResultType(result.getResultType());
        vo.setSourceTestCaseId(result.getSourceTestCaseId());
        vo.setTargetTestCaseId(result.getTargetTestCaseId());
        vo.setSourceCaseTitle(result.getSourceCaseTitle());
        vo.setTargetCaseTitle(result.getTargetCaseTitle());
        vo.setChangeSummary(result.getChangeSummary());
        vo.setFieldDiffs(result.getFieldDiffs());
        vo.setCreateTime(result.getCreateTime());

        return vo;
    }

    private TestCaseVersionHistoryVO toHistoryVO(TestCaseVersionHistory history) {
        TestCaseVersionHistoryVO vo = new TestCaseVersionHistoryVO();

        vo.setId(history.getId());
        vo.setHistoryId(history.getHistoryId());
        vo.setCompareTaskId(history.getCompareTaskId());
        vo.setSnapshotType(history.getSnapshotType());
        vo.setCaseSetId(history.getCaseSetId());
        vo.setTestCaseId(history.getTestCaseId());
        vo.setProjectId(history.getProjectId());
        vo.setVersionNo(history.getVersionNo());
        vo.setModuleCode(history.getModuleCode());
        vo.setCaseTitle(history.getCaseTitle());
        vo.setCaseType(history.getCaseType());
        vo.setPriority(history.getPriority());
        vo.setPrecondition(history.getPrecondition());
        vo.setSteps(history.getSteps());
        vo.setExpectedResult(history.getExpectedResult());
        vo.setTestData(history.getTestData());
        vo.setSourceReferences(history.getSourceReferences());
        vo.setRiskPoint(history.getRiskPoint());
        vo.setAutomationSuggestion(history.getAutomationSuggestion());
        vo.setSourceType(history.getSourceType());
        vo.setReviewStatus(history.getReviewStatus());
        vo.setContentHash(history.getContentHash());
        vo.setCreateTime(history.getCreateTime());

        return vo;
    }

    private static class CaseSetCase {

        private TestCaseSetItem item;

        private TestCase testCase;

        private String compareKey;
    }
}