package com.zy.testpilotai.testcase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.llm.embedding.EmbeddingClient;
import com.zy.testpilotai.testcase.mapper.TestCaseMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSemanticDeduplicateTaskMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSemanticDuplicateResultMapper;
import com.zy.testpilotai.testcase.model.dto.TestCaseEmbeddingBuildRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSemanticDeduplicateRequest;
import com.zy.testpilotai.testcase.model.entity.TestCase;
import com.zy.testpilotai.testcase.model.entity.TestCaseSemanticDeduplicateTask;
import com.zy.testpilotai.testcase.model.entity.TestCaseSemanticDuplicateResult;
import com.zy.testpilotai.testcase.model.vo.TestCaseEmbeddingBuildResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseSemanticDeduplicateResultVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseSemanticDuplicatePairVO;
import com.zy.testpilotai.testcase.service.TestCaseSemanticDeduplicateService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestCaseSemanticDeduplicateServiceImpl implements TestCaseSemanticDeduplicateService {

    private final TestCaseMapper testCaseMapper;

    private final TestCaseSemanticDeduplicateTaskMapper taskMapper;

    private final TestCaseSemanticDuplicateResultMapper resultMapper;

    private final EmbeddingClient embeddingClient;

    private final JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseEmbeddingBuildResultVO buildEmbeddings(TestCaseEmbeddingBuildRequest request) {
        List<TestCase> testCases = listTestCases(
                request == null ? null : request.getTaskId(),
                request == null ? null : request.getProjectId(),
                request == null ? null : request.getVersionNo(),
                request == null ? null : request.getModuleCode()
        );

        boolean rebuild = request != null && Boolean.TRUE.equals(request.getRebuild());

        int successCount = 0;
        int skippedCount = 0;
        int failedCount = 0;

        for (TestCase testCase : testCases) {
            String content = buildEmbeddingContent(testCase);
            String contentHash = sha256(content);

            if (!rebuild && hasValidEmbedding(testCase.getId(), contentHash)) {
                skippedCount++;
                continue;
            }

            try {
                List<Float> vector = embeddingClient.embed(content);

                upsertEmbedding(
                        testCase,
                        contentHash,
                        vector,
                        "SUCCESS",
                        null
                );

                successCount++;
            } catch (Exception e) {
                upsertEmbedding(
                        testCase,
                        contentHash,
                        null,
                        "FAILED",
                        e.getMessage()
                );

                failedCount++;
            }
        }

        TestCaseEmbeddingBuildResultVO resultVO = new TestCaseEmbeddingBuildResultVO();
        resultVO.setTotalCaseCount(testCases.size());
        resultVO.setSuccessCount(successCount);
        resultVO.setSkippedCount(skippedCount);
        resultVO.setFailedCount(failedCount);
        resultVO.setEmbeddingModel(embeddingClient.modelName());
        resultVO.setEmbeddingDimension(embeddingClient.dimension());

        return resultVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseSemanticDeduplicateResultVO deduplicate(TestCaseSemanticDeduplicateRequest request) {
        validateDeduplicateRequest(request);

        String compareScope = normalizeCompareScope(request.getCompareScope());
        double threshold = request.getThreshold() == null ? 0.85 : request.getThreshold();
        int topK = request.getTopK() == null || request.getTopK() <= 0 ? 5 : request.getTopK();
        boolean rebuildEmbedding = Boolean.TRUE.equals(request.getRebuildEmbedding());

        String deduplicateTaskId = "sd_" + UUID.randomUUID().toString().replace("-", "");

        TestCaseSemanticDeduplicateTask task = createTask(
                deduplicateTaskId,
                request,
                compareScope,
                threshold,
                topK,
                rebuildEmbedding
        );

        try {
            TestCaseEmbeddingBuildRequest buildRequest = new TestCaseEmbeddingBuildRequest();
            buildRequest.setTaskId(request.getTaskId());
            buildRequest.setProjectId(request.getProjectId());
            buildRequest.setVersionNo(request.getVersionNo());
            buildRequest.setModuleCode(request.getModuleCode());
            buildRequest.setRebuild(rebuildEmbedding);

            TestCaseEmbeddingBuildResultVO buildResult = buildEmbeddings(buildRequest);

            List<TestCase> sourceCases = listSourceCases(request);
            int candidateCount = countCandidateCases(request, compareScope);

            Set<String> seenPairKeys = new HashSet<>();

            int duplicatePairCount = 0;
            int markedDuplicateCount = 0;

            for (TestCase sourceCase : sourceCases) {
                String sourceVector = getEmbeddingVectorString(sourceCase.getId());

                if (!StringUtils.hasText(sourceVector)) {
                    continue;
                }

                List<SemanticCandidate> candidates = searchSimilarCandidates(
                        sourceCase,
                        sourceVector,
                        request,
                        compareScope,
                        threshold,
                        topK
                );

                for (SemanticCandidate candidate : candidates) {
                    if (sourceCase.getId().equals(candidate.testCaseId())) {
                        continue;
                    }

                    String pairKey = buildPairKey(sourceCase.getId(), candidate.testCaseId());

                    if (!seenPairKeys.add(pairKey)) {
                        continue;
                    }

                    TestCase targetCase = testCaseMapper.selectById(candidate.testCaseId());

                    if (targetCase == null) {
                        continue;
                    }

                    String reason = "语义相似度达到 "
                            + round(candidate.similarity())
                            + "，超过阈值 "
                            + threshold;

                    Long duplicateCaseId = decideDuplicateCaseId(
                            sourceCase,
                            targetCase,
                            compareScope
                    );

                    Long originalCaseId = sourceCase.getId().equals(duplicateCaseId)
                            ? targetCase.getId()
                            : sourceCase.getId();

                    boolean marked = markDuplicateCase(
                            duplicateCaseId,
                            originalCaseId,
                            candidate.similarity(),
                            reason
                    );

                    TestCaseSemanticDuplicateResult result = new TestCaseSemanticDuplicateResult();
                    result.setDeduplicateTaskId(deduplicateTaskId);
                    result.setSourceTestCaseId(sourceCase.getId());
                    result.setTargetTestCaseId(targetCase.getId());
                    result.setSourceCaseTitle(sourceCase.getCaseTitle());
                    result.setTargetCaseTitle(targetCase.getCaseTitle());
                    result.setSimilarity(candidate.similarity());
                    result.setCompareScope(compareScope);
                    result.setMarkedDuplicate(marked ? 1 : 0);
                    result.setDuplicateReason(reason);
                    result.setCreateTime(LocalDateTime.now());

                    resultMapper.insert(result);

                    duplicatePairCount++;

                    if (marked) {
                        markedDuplicateCount++;
                    }
                }
            }

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("deduplicateTaskId", deduplicateTaskId);
            summary.put("compareScope", compareScope);
            summary.put("threshold", threshold);
            summary.put("topK", topK);
            summary.put("sourceCaseCount", sourceCases.size());
            summary.put("candidateCaseCount", candidateCount);
            summary.put("duplicatePairCount", duplicatePairCount);
            summary.put("markedDuplicateCount", markedDuplicateCount);
            summary.put("embeddingBuild", buildResult);

            task.setStatus("SUCCESS");
            task.setSourceCaseCount(sourceCases.size());
            task.setCandidateCaseCount(candidateCount);
            task.setDuplicatePairCount(duplicatePairCount);
            task.setMarkedDuplicateCount(markedDuplicateCount);
            task.setSummary(toJson(summary));
            task.setUpdateTime(LocalDateTime.now());

            taskMapper.updateById(task);

            return detail(deduplicateTaskId);
        } catch (BusinessException e) {
            markTaskFailed(task, e.getMessage());
            throw e;
        } catch (Exception e) {
            markTaskFailed(task, e.getMessage());
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "测试用例语义去重失败：" + e.getMessage()
            );
        }
    }

    @Override
    public TestCaseSemanticDeduplicateResultVO detail(String deduplicateTaskId) {
        if (!StringUtils.hasText(deduplicateTaskId)) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "语义去重任务 ID 不能为空"
            );
        }

        TestCaseSemanticDeduplicateTask task = taskMapper.selectOne(
                new LambdaQueryWrapper<TestCaseSemanticDeduplicateTask>()
                        .eq(TestCaseSemanticDeduplicateTask::getDeduplicateTaskId, deduplicateTaskId)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "语义去重任务不存在"
            );
        }

        List<TestCaseSemanticDuplicatePairVO> pairs = resultMapper.selectList(
                        new LambdaQueryWrapper<TestCaseSemanticDuplicateResult>()
                                .eq(TestCaseSemanticDuplicateResult::getDeduplicateTaskId, deduplicateTaskId)
                                .orderByDesc(TestCaseSemanticDuplicateResult::getSimilarity)
                                .orderByAsc(TestCaseSemanticDuplicateResult::getId)
                )
                .stream()
                .map(this::toPairVO)
                .toList();

        TestCaseSemanticDeduplicateResultVO vo = new TestCaseSemanticDeduplicateResultVO();

        vo.setDeduplicateTaskId(task.getDeduplicateTaskId());
        vo.setCompareScope(task.getCompareScope());
        vo.setTaskId(task.getTaskId());
        vo.setProjectId(task.getProjectId());
        vo.setVersionNo(task.getVersionNo());
        vo.setModuleCode(task.getModuleCode());
        vo.setThreshold(task.getThreshold());
        vo.setTopK(task.getTopK());
        vo.setRebuildEmbedding(task.getRebuildEmbedding());
        vo.setStatus(task.getStatus());
        vo.setSourceCaseCount(task.getSourceCaseCount());
        vo.setCandidateCaseCount(task.getCandidateCaseCount());
        vo.setDuplicatePairCount(task.getDuplicatePairCount());
        vo.setMarkedDuplicateCount(task.getMarkedDuplicateCount());
        vo.setSummary(task.getSummary());
        vo.setErrorMessage(task.getErrorMessage());
        vo.setCreateTime(task.getCreateTime());
        vo.setUpdateTime(task.getUpdateTime());
        vo.setDuplicatePairs(pairs);

        return vo;
    }

    private void validateDeduplicateRequest(TestCaseSemanticDeduplicateRequest request) {
        if (request == null) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "语义去重请求不能为空"
            );
        }

        if (!StringUtils.hasText(request.getTaskId())
                && request.getProjectId() == null) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "taskId 和 projectId 至少传一个"
            );
        }

        if (request.getThreshold() != null
                && (request.getThreshold() <= 0 || request.getThreshold() > 1)) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "相似度阈值必须在 0 - 1 之间"
            );
        }
    }

    private TestCaseSemanticDeduplicateTask createTask(
            String deduplicateTaskId,
            TestCaseSemanticDeduplicateRequest request,
            String compareScope,
            double threshold,
            int topK,
            boolean rebuildEmbedding
    ) {
        TestCaseSemanticDeduplicateTask task = new TestCaseSemanticDeduplicateTask();

        task.setDeduplicateTaskId(deduplicateTaskId);
        task.setCompareScope(compareScope);
        task.setTaskId(request.getTaskId());
        task.setProjectId(request.getProjectId());
        task.setVersionNo(request.getVersionNo());
        task.setModuleCode(request.getModuleCode());
        task.setThreshold(threshold);
        task.setTopK(topK);
        task.setRebuildEmbedding(rebuildEmbedding ? 1 : 0);
        task.setStatus("RUNNING");
        task.setSourceCaseCount(0);
        task.setCandidateCaseCount(0);
        task.setDuplicatePairCount(0);
        task.setMarkedDuplicateCount(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        taskMapper.insert(task);

        return task;
    }

    private List<TestCase> listSourceCases(TestCaseSemanticDeduplicateRequest request) {
        return listTestCases(
                request.getTaskId(),
                request.getProjectId(),
                request.getVersionNo(),
                request.getModuleCode()
        );
    }

    private List<TestCase> listTestCases(
            String taskId,
            Long projectId,
            String versionNo,
            String moduleCode
    ) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<TestCase>()
                .orderByAsc(TestCase::getId);

        if (StringUtils.hasText(taskId)) {
            wrapper.eq(TestCase::getTaskId, taskId);
        }

        if (projectId != null) {
            wrapper.eq(TestCase::getProjectId, projectId);
        }

        if (StringUtils.hasText(versionNo)) {
            wrapper.eq(TestCase::getVersionNo, versionNo);
        }

        if (StringUtils.hasText(moduleCode)) {
            wrapper.eq(TestCase::getModuleCode, moduleCode);
        }

        wrapper.and(w -> w
                .ne(TestCase::getReviewStatus, "DELETED")
                .or()
                .isNull(TestCase::getReviewStatus)
        );

        return testCaseMapper.selectList(wrapper);
    }

    private int countCandidateCases(
            TestCaseSemanticDeduplicateRequest request,
            String compareScope
    ) {
        StringBuilder sql = new StringBuilder(
                "SELECT COUNT(*) FROM test_case_embedding e " +
                        "JOIN test_case t ON t.id = e.test_case_id " +
                        "WHERE e.status = 'SUCCESS' "
        );

        List<Object> params = new ArrayList<>();

        appendCandidateScopeCondition(sql, params, request, compareScope, null);

        Integer count = jdbcTemplate.queryForObject(
                sql.toString(),
                Integer.class,
                params.toArray()
        );

        return count == null ? 0 : count;
    }

    private List<SemanticCandidate> searchSimilarCandidates(
            TestCase sourceCase,
            String sourceVector,
            TestCaseSemanticDeduplicateRequest request,
            String compareScope,
            double threshold,
            int topK
    ) {
        StringBuilder sql = new StringBuilder(
                """
                SELECT
                    e.test_case_id,
                    e.case_title,
                    1 - (e.embedding <=> ?::vector) AS similarity
                FROM test_case_embedding e
                JOIN test_case t ON t.id = e.test_case_id
                WHERE e.status = 'SUCCESS'
                  AND e.embedding IS NOT NULL
                  AND e.test_case_id <> ?
                  AND (t.review_status IS NULL OR t.review_status <> 'DELETED')
                """
        );

        List<Object> params = new ArrayList<>();

        params.add(sourceVector);
        params.add(sourceCase.getId());

        appendCandidateScopeCondition(sql, params, request, compareScope, sourceCase);

        sql.append(" AND (1 - (e.embedding <=> ?::vector)) >= ? ");
        params.add(sourceVector);
        params.add(threshold);

        sql.append(" ORDER BY e.embedding <=> ?::vector ASC LIMIT ? ");
        params.add(sourceVector);
        params.add(topK);

        return jdbcTemplate.query(
                sql.toString(),
                (rs, rowNum) -> new SemanticCandidate(
                        rs.getLong("test_case_id"),
                        rs.getString("case_title"),
                        rs.getDouble("similarity")
                ),
                params.toArray()
        );
    }

    private void appendCandidateScopeCondition(
            StringBuilder sql,
            List<Object> params,
            TestCaseSemanticDeduplicateRequest request,
            String compareScope,
            TestCase sourceCase
    ) {
        if ("TASK".equals(compareScope)) {
            if (!StringUtils.hasText(request.getTaskId())) {
                throw new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "TASK 范围去重必须传 taskId"
                );
            }

            sql.append(" AND e.task_id = ? ");
            params.add(request.getTaskId());
            return;
        }

        if ("VERSION".equals(compareScope)) {
            Long projectId = request.getProjectId() == null && sourceCase != null
                    ? sourceCase.getProjectId()
                    : request.getProjectId();

            String versionNo = StringUtils.hasText(request.getVersionNo())
                    ? request.getVersionNo()
                    : sourceCase == null ? null : sourceCase.getVersionNo();

            if (projectId == null || !StringUtils.hasText(versionNo)) {
                throw new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "VERSION 范围去重必须传 projectId 和 versionNo"
                );
            }

            sql.append(" AND e.project_id = ? AND e.version_no = ? ");
            params.add(projectId);
            params.add(versionNo);

            String moduleCode = StringUtils.hasText(request.getModuleCode())
                    ? request.getModuleCode()
                    : sourceCase == null ? null : sourceCase.getModuleCode();

            if (StringUtils.hasText(moduleCode)) {
                sql.append(" AND e.module_code = ? ");
                params.add(moduleCode);
            }

            return;
        }

        if ("PROJECT".equals(compareScope)) {
            Long projectId = request.getProjectId() == null && sourceCase != null
                    ? sourceCase.getProjectId()
                    : request.getProjectId();

            if (projectId == null) {
                throw new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "PROJECT 范围去重必须传 projectId"
                );
            }

            sql.append(" AND e.project_id = ? ");
            params.add(projectId);

            if (StringUtils.hasText(request.getModuleCode())) {
                sql.append(" AND e.module_code = ? ");
                params.add(request.getModuleCode());
            }

            return;
        }

        if ("CROSS_VERSION".equals(compareScope)) {
            Long projectId = request.getProjectId() == null && sourceCase != null
                    ? sourceCase.getProjectId()
                    : request.getProjectId();

            String moduleCode = StringUtils.hasText(request.getModuleCode())
                    ? request.getModuleCode()
                    : sourceCase == null ? null : sourceCase.getModuleCode();

            String sourceVersionNo = StringUtils.hasText(request.getVersionNo())
                    ? request.getVersionNo()
                    : sourceCase == null ? null : sourceCase.getVersionNo();

            if (projectId == null) {
                throw new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "CROSS_VERSION 范围去重必须传 projectId"
                );
            }

            sql.append(" AND e.project_id = ? ");
            params.add(projectId);

            if (StringUtils.hasText(moduleCode)) {
                sql.append(" AND e.module_code = ? ");
                params.add(moduleCode);
            }

            if (StringUtils.hasText(sourceVersionNo)) {
                sql.append(" AND (e.version_no IS NULL OR e.version_no <> ?) ");
                params.add(sourceVersionNo);
            }
        }
    }

    private boolean hasValidEmbedding(Long testCaseId, String contentHash) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM test_case_embedding
                WHERE test_case_id = ?
                  AND content_hash = ?
                  AND status = 'SUCCESS'
                  AND embedding IS NOT NULL
                """,
                Integer.class,
                testCaseId,
                contentHash
        );

        return count != null && count > 0;
    }

    private void upsertEmbedding(
            TestCase testCase,
            String contentHash,
            List<Float> vector,
            String status,
            String errorMessage
    ) {
        String vectorText = vector == null ? null : toVectorText(vector);

        jdbcTemplate.update(
                """
                INSERT INTO test_case_embedding (
                    test_case_id,
                    task_id,
                    project_id,
                    version_no,
                    module_code,
                    case_title,
                    content_hash,
                    embedding_model,
                    embedding_dimension,
                    embedding,
                    status,
                    error_message,
                    create_time,
                    update_time
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::vector, ?, ?, NOW(), NOW()
                )
                ON CONFLICT (test_case_id)
                DO UPDATE SET
                    task_id = EXCLUDED.task_id,
                    project_id = EXCLUDED.project_id,
                    version_no = EXCLUDED.version_no,
                    module_code = EXCLUDED.module_code,
                    case_title = EXCLUDED.case_title,
                    content_hash = EXCLUDED.content_hash,
                    embedding_model = EXCLUDED.embedding_model,
                    embedding_dimension = EXCLUDED.embedding_dimension,
                    embedding = EXCLUDED.embedding,
                    status = EXCLUDED.status,
                    error_message = EXCLUDED.error_message,
                    update_time = NOW()
                """,
                testCase.getId(),
                testCase.getTaskId(),
                testCase.getProjectId(),
                testCase.getVersionNo(),
                testCase.getModuleCode(),
                testCase.getCaseTitle(),
                contentHash,
                embeddingClient.modelName(),
                embeddingClient.dimension(),
                vectorText,
                status,
                errorMessage
        );
    }

    private String getEmbeddingVectorString(Long testCaseId) {
        List<String> values = jdbcTemplate.query(
                """
                SELECT embedding::text AS embedding_text
                FROM test_case_embedding
                WHERE test_case_id = ?
                  AND status = 'SUCCESS'
                  AND embedding IS NOT NULL
                LIMIT 1
                """,
                (rs, rowNum) -> rs.getString("embedding_text"),
                testCaseId
        );

        return values.isEmpty() ? null : values.get(0);
    }

    private boolean markDuplicateCase(
            Long duplicateCaseId,
            Long originalCaseId,
            double similarity,
            String reason
    ) {
        TestCase duplicateCase = testCaseMapper.selectById(duplicateCaseId);

        if (duplicateCase == null) {
            return false;
        }

        if ("DELETED".equals(duplicateCase.getReviewStatus())) {
            return false;
        }

        duplicateCase.setDuplicateStatus("DUPLICATE");
        duplicateCase.setDuplicateOfCaseId(originalCaseId);
        duplicateCase.setDuplicateScore(similarity);
        duplicateCase.setDuplicateReason(reason);
        duplicateCase.setUpdateTime(LocalDateTime.now());

        testCaseMapper.updateById(duplicateCase);

        return true;
    }

    private Long decideDuplicateCaseId(
            TestCase sourceCase,
            TestCase targetCase,
            String compareScope
    ) {
        if (!"TASK".equals(compareScope)) {
            return sourceCase.getId();
        }

        if (targetCase.getId() == null || sourceCase.getId() == null) {
            return sourceCase.getId();
        }

        return sourceCase.getId() > targetCase.getId()
                ? sourceCase.getId()
                : targetCase.getId();
    }

    private String normalizeCompareScope(String compareScope) {
        if (!StringUtils.hasText(compareScope)) {
            return "TASK";
        }

        String value = compareScope.trim().toUpperCase();

        return switch (value) {
            case "TASK", "VERSION", "PROJECT", "CROSS_VERSION" -> value;
            default -> "TASK";
        };
    }

    private String buildEmbeddingContent(TestCase testCase) {
        StringBuilder builder = new StringBuilder();

        append(builder, "模块编码", testCase.getModuleCode());
        append(builder, "模块名称", testCase.getModuleName());
        append(builder, "用例标题", testCase.getCaseTitle());
        append(builder, "用例类型", testCase.getCaseType());
        append(builder, "优先级", testCase.getPriority());
        append(builder, "前置条件", testCase.getPrecondition());
        append(builder, "测试步骤", testCase.getSteps());
        append(builder, "预期结果", testCase.getExpectedResult());
        append(builder, "测试数据", testCase.getTestData());
        append(builder, "风险点", testCase.getRiskPoint());
        append(builder, "自动化建议", testCase.getAutomationSuggestion());

        return builder.toString();
    }

    private void append(StringBuilder builder, String label, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }

        builder.append(label)
                .append("：")
                .append(value)
                .append("\n");
    }

    private String buildPairKey(Long a, Long b) {
        long min = Math.min(a, b);
        long max = Math.max(a, b);

        return min + ":" + max;
    }

    private String toVectorText(List<Float> vector) {
        StringBuilder builder = new StringBuilder();
        builder.append("[");

        for (int i = 0; i < vector.size(); i++) {
            if (i > 0) {
                builder.append(",");
            }

            builder.append(vector.get(i));
        }

        builder.append("]");

        return builder.toString();
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
                    "生成 SHA-256 失败：" + e.getMessage()
            );
        }
    }

    private void markTaskFailed(
            TestCaseSemanticDeduplicateTask task,
            String errorMessage
    ) {
        task.setStatus("FAILED");
        task.setErrorMessage(errorMessage);
        task.setUpdateTime(LocalDateTime.now());

        taskMapper.updateById(task);
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

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
    }

    private TestCaseSemanticDuplicatePairVO toPairVO(TestCaseSemanticDuplicateResult result) {
        TestCaseSemanticDuplicatePairVO vo = new TestCaseSemanticDuplicatePairVO();

        vo.setId(result.getId());
        vo.setDeduplicateTaskId(result.getDeduplicateTaskId());
        vo.setSourceTestCaseId(result.getSourceTestCaseId());
        vo.setTargetTestCaseId(result.getTargetTestCaseId());
        vo.setSourceCaseTitle(result.getSourceCaseTitle());
        vo.setTargetCaseTitle(result.getTargetCaseTitle());
        vo.setSimilarity(result.getSimilarity());
        vo.setCompareScope(result.getCompareScope());
        vo.setMarkedDuplicate(result.getMarkedDuplicate());
        vo.setDuplicateReason(result.getDuplicateReason());
        vo.setCreateTime(result.getCreateTime());

        return vo;
    }

    private record SemanticCandidate(
            Long testCaseId,
            String caseTitle,
            Double similarity
    ) {
    }
}