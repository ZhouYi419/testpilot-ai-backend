package com.zy.testpilotai.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.knowledge.mapper.KnowledgeEvaluateItemMapper;
import com.zy.testpilotai.knowledge.mapper.KnowledgeEvaluateTaskMapper;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeEvaluateQueryRequest;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeEvaluateRequest;
import com.zy.testpilotai.knowledge.model.entity.KnowledgeEvaluateItem;
import com.zy.testpilotai.knowledge.model.entity.KnowledgeEvaluateTask;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeEvaluateItemVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeEvaluateResultVO;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeEvaluateTaskVO;
import com.zy.testpilotai.knowledge.service.KnowledgeEvaluateService;
import com.zy.testpilotai.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KnowledgeEvaluateServiceImpl implements KnowledgeEvaluateService {

    private static final String DOCUMENT_TABLE = "prd_document";

    private static final String CHUNK_TABLE = "knowledge_chunk";

    private final ProjectService projectService;

    private final KnowledgeEvaluateTaskMapper taskMapper;

    private final KnowledgeEvaluateItemMapper itemMapper;

    private final JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public KnowledgeEvaluateResultVO evaluate(KnowledgeEvaluateRequest request) {
        validateRequest(request);

        projectService.getById(request.getProjectId());

        String evaluateTaskId = "ke_" + UUID.randomUUID().toString().replace("-", "");

        KnowledgeEvaluateTask task = createTask(evaluateTaskId, request);

        try {
            boolean documentTableExists = tableExists(DOCUMENT_TABLE);
            boolean chunkTableExists = tableExists(CHUNK_TABLE);

            int documentCount = documentTableExists
                    ? countDocuments(request)
                    : 0;

            int chunkCount = chunkTableExists
                    ? countChunks(request)
                    : 0;

            int parentChunkCount = chunkTableExists
                    ? countChunksByType(request, "PARENT")
                    : 0;

            int childChunkCount = chunkTableExists
                    ? countChunksByType(request, "CHILD")
                    : 0;

            int embeddingMissingCount = chunkTableExists
                    ? countEmbeddingMissing(request)
                    : 0;

            int moduleMissingCount = chunkTableExists
                    ? countModuleMissing(request)
                    : 0;

            int tooShortChunkCount = chunkTableExists
                    ? countTooShortChunks(request)
                    : 0;

            int tooLongChunkCount = chunkTableExists
                    ? countTooLongChunks(request)
                    : 0;

            int orphanChildChunkCount = chunkTableExists
                    ? countOrphanChildChunks(request)
                    : 0;

            int queryHitCount = chunkTableExists
                    ? countQueryHits(request)
                    : 0;

            addDocumentItem(evaluateTaskId, documentTableExists, documentCount);
            addChunkCountItem(evaluateTaskId, chunkTableExists, chunkCount, parentChunkCount, childChunkCount);
            addChunkLengthItem(evaluateTaskId, chunkCount, tooShortChunkCount, tooLongChunkCount);
            addParentChildItem(evaluateTaskId, chunkTableExists, childChunkCount, orphanChildChunkCount);
            addEmbeddingItem(evaluateTaskId, chunkTableExists, chunkCount, embeddingMissingCount);
            addModuleCoverageItem(evaluateTaskId, chunkTableExists, chunkCount, moduleMissingCount);
            addVersionCoverageItem(evaluateTaskId, chunkTableExists, request);
            addQueryHitItem(evaluateTaskId, request, queryHitCount);

            List<KnowledgeEvaluateItem> items = listItems(evaluateTaskId);

            double totalScore = calculateTotalScore(items);

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("documentTableExists", documentTableExists);
            summary.put("chunkTableExists", chunkTableExists);
            summary.put("documentCount", documentCount);
            summary.put("chunkCount", chunkCount);
            summary.put("parentChunkCount", parentChunkCount);
            summary.put("childChunkCount", childChunkCount);
            summary.put("embeddingMissingCount", embeddingMissingCount);
            summary.put("moduleMissingCount", moduleMissingCount);
            summary.put("tooShortChunkCount", tooShortChunkCount);
            summary.put("tooLongChunkCount", tooLongChunkCount);
            summary.put("orphanChildChunkCount", orphanChildChunkCount);
            summary.put("queryHitCount", queryHitCount);
            summary.put("totalScore", totalScore);

            task.setStatus("SUCCESS");
            task.setTotalScore(totalScore);
            task.setDocumentCount(documentCount);
            task.setChunkCount(chunkCount);
            task.setParentChunkCount(parentChunkCount);
            task.setChildChunkCount(childChunkCount);
            task.setEmbeddingMissingCount(embeddingMissingCount);
            task.setModuleMissingCount(moduleMissingCount);
            task.setTooShortChunkCount(tooShortChunkCount);
            task.setTooLongChunkCount(tooLongChunkCount);
            task.setOrphanChildChunkCount(orphanChildChunkCount);
            task.setSummary(toJson(summary));
            task.setUpdateTime(LocalDateTime.now());

            taskMapper.updateById(task);

            return detail(evaluateTaskId);
        } catch (BusinessException e) {
            markFailed(task, e.getMessage());
            throw e;
        } catch (Exception e) {
            markFailed(task, e.getMessage());
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "知识库质量评估失败：" + e.getMessage()
            );
        }
    }

    @Override
    public List<KnowledgeEvaluateTaskVO> list(KnowledgeEvaluateQueryRequest request) {
        LambdaQueryWrapper<KnowledgeEvaluateTask> wrapper =
                new LambdaQueryWrapper<KnowledgeEvaluateTask>()
                        .orderByDesc(KnowledgeEvaluateTask::getCreateTime)
                        .orderByDesc(KnowledgeEvaluateTask::getId);

        if (request != null && request.getProjectId() != null) {
            wrapper.eq(KnowledgeEvaluateTask::getProjectId, request.getProjectId());
        }

        if (request != null && StringUtils.hasText(request.getVersionNo())) {
            wrapper.eq(KnowledgeEvaluateTask::getVersionNo, request.getVersionNo());
        }

        if (request != null && StringUtils.hasText(request.getModuleCode())) {
            wrapper.eq(KnowledgeEvaluateTask::getModuleCode, request.getModuleCode());
        }

        if (request != null && StringUtils.hasText(request.getStatus())) {
            wrapper.eq(KnowledgeEvaluateTask::getStatus, request.getStatus());
        }

        return taskMapper.selectList(wrapper)
                .stream()
                .map(this::toTaskVO)
                .toList();
    }

    @Override
    public KnowledgeEvaluateResultVO detail(String evaluateTaskId) {
        if (!StringUtils.hasText(evaluateTaskId)) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "评估任务 ID 不能为空"
            );
        }

        KnowledgeEvaluateTask task = taskMapper.selectOne(
                new LambdaQueryWrapper<KnowledgeEvaluateTask>()
                        .eq(KnowledgeEvaluateTask::getEvaluateTaskId, evaluateTaskId)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "知识库评估任务不存在"
            );
        }

        List<KnowledgeEvaluateItemVO> items = listItems(evaluateTaskId)
                .stream()
                .map(this::toItemVO)
                .toList();

        KnowledgeEvaluateResultVO resultVO = new KnowledgeEvaluateResultVO();
        resultVO.setTask(toTaskVO(task));
        resultVO.setItems(items);

        return resultVO;
    }

    private void validateRequest(KnowledgeEvaluateRequest request) {
        if (request == null || request.getProjectId() == null) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "项目 ID 不能为空"
            );
        }
    }

    private KnowledgeEvaluateTask createTask(
            String evaluateTaskId,
            KnowledgeEvaluateRequest request
    ) {
        KnowledgeEvaluateTask task = new KnowledgeEvaluateTask();

        task.setEvaluateTaskId(evaluateTaskId);
        task.setProjectId(request.getProjectId());
        task.setVersionNo(request.getVersionNo());
        task.setModuleCode(request.getModuleCode());
        task.setQueryText(request.getQueryText());
        task.setStatus("RUNNING");
        task.setTotalScore(0.0);
        task.setDocumentCount(0);
        task.setChunkCount(0);
        task.setParentChunkCount(0);
        task.setChildChunkCount(0);
        task.setEmbeddingMissingCount(0);
        task.setModuleMissingCount(0);
        task.setTooShortChunkCount(0);
        task.setTooLongChunkCount(0);
        task.setOrphanChildChunkCount(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        taskMapper.insert(task);

        return task;
    }

    private boolean tableExists(String tableName) {
        Boolean exists = jdbcTemplate.queryForObject(
                "SELECT to_regclass(?) IS NOT NULL",
                Boolean.class,
                "public." + tableName
        );

        return Boolean.TRUE.equals(exists);
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = ?
                  AND column_name = ?
                """,
                Integer.class,
                tableName,
                columnName
        );

        return count != null && count > 0;
    }

    private int countDocuments(KnowledgeEvaluateRequest request) {
        if (!tableExists(DOCUMENT_TABLE)) {
            return 0;
        }

        SqlBuilder sql = new SqlBuilder()
                .append("SELECT COUNT(*) FROM " + DOCUMENT_TABLE + " WHERE project_id = ?", request.getProjectId());

        if (StringUtils.hasText(request.getVersionNo()) && columnExists(DOCUMENT_TABLE, "version_no")) {
            sql.append(" AND version_no = ?", request.getVersionNo());
        }

        return queryInt(sql);
    }

    private int countChunks(KnowledgeEvaluateRequest request) {
        SqlBuilder sql = baseChunkCountSql(request);
        return queryInt(sql);
    }

    private int countChunksByType(KnowledgeEvaluateRequest request, String chunkType) {
        if (!columnExists(CHUNK_TABLE, "chunk_type")) {
            return 0;
        }

        SqlBuilder sql = baseChunkCountSql(request)
                .append(" AND UPPER(chunk_type) = ?", chunkType);

        return queryInt(sql);
    }

    private int countEmbeddingMissing(KnowledgeEvaluateRequest request) {
        if (!columnExists(CHUNK_TABLE, "embedding")) {
            return 0;
        }

        SqlBuilder sql = baseChunkCountSql(request)
                .append(" AND embedding IS NULL");

        return queryInt(sql);
    }

    private int countModuleMissing(KnowledgeEvaluateRequest request) {
        if (!columnExists(CHUNK_TABLE, "module_code")) {
            return 0;
        }

        SqlBuilder sql = baseChunkCountSql(request)
                .append(" AND (module_code IS NULL OR module_code = '')");

        return queryInt(sql);
    }

    private int countTooShortChunks(KnowledgeEvaluateRequest request) {
        int minLength = request.getMinChunkLength() == null
                ? 80
                : request.getMinChunkLength();

        if (columnExists(CHUNK_TABLE, "token_count")) {
            SqlBuilder sql = baseChunkCountSql(request)
                    .append(" AND token_count < ?", minLength);
            return queryInt(sql);
        }

        if (columnExists(CHUNK_TABLE, "content")) {
            SqlBuilder sql = baseChunkCountSql(request)
                    .append(" AND LENGTH(content) < ?", minLength);
            return queryInt(sql);
        }

        return 0;
    }

    private int countTooLongChunks(KnowledgeEvaluateRequest request) {
        int maxLength = request.getMaxChunkLength() == null
                ? 3000
                : request.getMaxChunkLength();

        if (columnExists(CHUNK_TABLE, "token_count")) {
            SqlBuilder sql = baseChunkCountSql(request)
                    .append(" AND token_count > ?", maxLength);
            return queryInt(sql);
        }

        if (columnExists(CHUNK_TABLE, "content")) {
            SqlBuilder sql = baseChunkCountSql(request)
                    .append(" AND LENGTH(content) > ?", maxLength);
            return queryInt(sql);
        }

        return 0;
    }

    private int countOrphanChildChunks(KnowledgeEvaluateRequest request) {
        if (!columnExists(CHUNK_TABLE, "chunk_type")
                || !columnExists(CHUNK_TABLE, "parent_chunk_id")) {
            return 0;
        }

        SqlBuilder sql = baseChunkCountSql(request)
                .append(" AND UPPER(chunk_type) = ?", "CHILD")
                .append(" AND parent_chunk_id IS NULL");

        return queryInt(sql);
    }

    private int countQueryHits(KnowledgeEvaluateRequest request) {
        if (!StringUtils.hasText(request.getQueryText())) {
            return 0;
        }

        if (!columnExists(CHUNK_TABLE, "content")) {
            return 0;
        }

        String keyword = firstKeyword(request.getQueryText());

        if (!StringUtils.hasText(keyword)) {
            return 0;
        }

        SqlBuilder sql = baseChunkCountSql(request)
                .append(" AND content ILIKE ?", "%" + keyword + "%");

        return queryInt(sql);
    }

    private SqlBuilder baseChunkCountSql(KnowledgeEvaluateRequest request) {
        SqlBuilder sql = new SqlBuilder()
                .append("SELECT COUNT(*) FROM " + CHUNK_TABLE + " WHERE project_id = ?", request.getProjectId());

        if (StringUtils.hasText(request.getVersionNo()) && columnExists(CHUNK_TABLE, "version_no")) {
            sql.append(" AND version_no = ?", request.getVersionNo());
        }

        if (StringUtils.hasText(request.getModuleCode()) && columnExists(CHUNK_TABLE, "module_code")) {
            sql.append(" AND module_code = ?", request.getModuleCode());
        }

        return sql;
    }

    private int queryInt(SqlBuilder sql) {
        Integer value = jdbcTemplate.queryForObject(
                sql.sql(),
                Integer.class,
                sql.params().toArray()
        );

        return value == null ? 0 : value;
    }

    private void addDocumentItem(
            String evaluateTaskId,
            boolean documentTableExists,
            int documentCount
    ) {
        if (!documentTableExists) {
            addItem(
                    evaluateTaskId,
                    "DOCUMENT",
                    "文档表检查",
                    "表不存在",
                    "FAIL",
                    0,
                    "未找到 PRD 文档表 " + DOCUMENT_TABLE,
                    "请确认文档上传解析流程是否创建了 " + DOCUMENT_TABLE + " 表，或修改评估服务中的 DOCUMENT_TABLE 常量。",
                    Map.of("table", DOCUMENT_TABLE)
            );
            return;
        }

        if (documentCount <= 0) {
            addItem(
                    evaluateTaskId,
                    "DOCUMENT",
                    "文档数量",
                    String.valueOf(documentCount),
                    "FAIL",
                    0,
                    "当前项目 / 版本下没有 PRD 文档。",
                    "请先上传 PRD 并完成解析，再执行知识库评估。",
                    Map.of("documentCount", documentCount)
            );
            return;
        }

        addItem(
                evaluateTaskId,
                "DOCUMENT",
                "文档数量",
                String.valueOf(documentCount),
                "PASS",
                100,
                "已检测到 PRD 文档。",
                "无需处理。",
                Map.of("documentCount", documentCount)
        );
    }

    private void addChunkCountItem(
            String evaluateTaskId,
            boolean chunkTableExists,
            int chunkCount,
            int parentChunkCount,
            int childChunkCount
    ) {
        if (!chunkTableExists) {
            addItem(
                    evaluateTaskId,
                    "CHUNK",
                    "Chunk 表检查",
                    "表不存在",
                    "FAIL",
                    0,
                    "未找到知识库 Chunk 表 " + CHUNK_TABLE,
                    "请确认知识库构建流程是否创建了 " + CHUNK_TABLE + " 表，或修改评估服务中的 CHUNK_TABLE 常量。",
                    Map.of("table", CHUNK_TABLE)
            );
            return;
        }

        if (chunkCount <= 0) {
            addItem(
                    evaluateTaskId,
                    "CHUNK",
                    "Chunk 数量",
                    String.valueOf(chunkCount),
                    "FAIL",
                    0,
                    "当前项目 / 版本下没有生成 Chunk。",
                    "请检查文档解析、切片流程和入库逻辑。",
                    Map.of("chunkCount", chunkCount)
            );
            return;
        }

        String status = childChunkCount <= 0 ? "WARN" : "PASS";
        double score = childChunkCount <= 0 ? 70 : 100;

        addItem(
                evaluateTaskId,
                "CHUNK",
                "Chunk 数量",
                String.valueOf(chunkCount),
                status,
                score,
                childChunkCount <= 0
                        ? "未检测到 Child Chunk，可能没有启用 Parent / Child 切片。"
                        : "Chunk 数量正常。",
                childChunkCount <= 0
                        ? "建议启用 Parent / Child Chunk，提高召回粒度和上下文完整度。"
                        : "无需处理。",
                Map.of(
                        "chunkCount", chunkCount,
                        "parentChunkCount", parentChunkCount,
                        "childChunkCount", childChunkCount
                )
        );
    }

    private void addChunkLengthItem(
            String evaluateTaskId,
            int chunkCount,
            int tooShortChunkCount,
            int tooLongChunkCount
    ) {
        if (chunkCount <= 0) {
            addItem(
                    evaluateTaskId,
                    "CHUNK_LENGTH",
                    "Chunk 长度",
                    "无 Chunk",
                    "FAIL",
                    0,
                    "没有 Chunk，无法评估长度。",
                    "请先完成知识库构建。",
                    Map.of()
            );
            return;
        }

        double badRatio = (tooShortChunkCount + tooLongChunkCount) * 1.0 / chunkCount;

        String status;
        double score;

        if (badRatio >= 0.3) {
            status = "FAIL";
            score = 40;
        } else if (badRatio >= 0.1) {
            status = "WARN";
            score = 75;
        } else {
            status = "PASS";
            score = 100;
        }

        addItem(
                evaluateTaskId,
                "CHUNK_LENGTH",
                "Chunk 长度",
                "过短=" + tooShortChunkCount + "，过长=" + tooLongChunkCount,
                status,
                score,
                "过短或过长 Chunk 占比：" + badRatio,
                "过短 Chunk 可能丢上下文，过长 Chunk 可能降低召回精度。建议调整切片规则。",
                Map.of(
                        "chunkCount", chunkCount,
                        "tooShortChunkCount", tooShortChunkCount,
                        "tooLongChunkCount", tooLongChunkCount,
                        "badRatio", badRatio
                )
        );
    }

    private void addParentChildItem(
            String evaluateTaskId,
            boolean chunkTableExists,
            int childChunkCount,
            int orphanChildChunkCount
    ) {
        if (!chunkTableExists) {
            return;
        }

        if (childChunkCount <= 0) {
            addItem(
                    evaluateTaskId,
                    "PARENT_CHILD",
                    "Parent / Child 结构",
                    "无 Child Chunk",
                    "WARN",
                    70,
                    "未检测到 Child Chunk。",
                    "建议使用 Child Chunk 做向量召回，Parent Chunk 做上下文补全。",
                    Map.of("childChunkCount", childChunkCount)
            );
            return;
        }

        if (orphanChildChunkCount > 0) {
            addItem(
                    evaluateTaskId,
                    "PARENT_CHILD",
                    "孤儿 Child Chunk",
                    String.valueOf(orphanChildChunkCount),
                    "FAIL",
                    40,
                    "存在没有 parent_chunk_id 的 Child Chunk。",
                    "请检查切片入库逻辑，确保 Child Chunk 能回溯到 Parent Chunk。",
                    Map.of("orphanChildChunkCount", orphanChildChunkCount)
            );
            return;
        }

        addItem(
                evaluateTaskId,
                "PARENT_CHILD",
                "Parent / Child 结构",
                "正常",
                "PASS",
                100,
                "Child Chunk 均有 Parent 关联。",
                "无需处理。",
                Map.of("childChunkCount", childChunkCount)
        );
    }

    private void addEmbeddingItem(
            String evaluateTaskId,
            boolean chunkTableExists,
            int chunkCount,
            int embeddingMissingCount
    ) {
        if (!chunkTableExists) {
            return;
        }

        if (chunkCount <= 0) {
            return;
        }

        double missingRatio = embeddingMissingCount * 1.0 / chunkCount;

        String status;
        double score;

        if (embeddingMissingCount == 0) {
            status = "PASS";
            score = 100;
        } else if (missingRatio < 0.1) {
            status = "WARN";
            score = 80;
        } else {
            status = "FAIL";
            score = 30;
        }

        addItem(
                evaluateTaskId,
                "EMBEDDING",
                "Embedding 完整率",
                "缺失=" + embeddingMissingCount,
                status,
                score,
                embeddingMissingCount == 0
                        ? "所有 Chunk 都已生成 Embedding。"
                        : "存在未生成 Embedding 的 Chunk。",
                "请检查 Embedding 调用日志、模型配置和重试机制。",
                Map.of(
                        "chunkCount", chunkCount,
                        "embeddingMissingCount", embeddingMissingCount,
                        "missingRatio", missingRatio
                )
        );
    }

    private void addModuleCoverageItem(
            String evaluateTaskId,
            boolean chunkTableExists,
            int chunkCount,
            int moduleMissingCount
    ) {
        if (!chunkTableExists || chunkCount <= 0) {
            return;
        }

        double missingRatio = moduleMissingCount * 1.0 / chunkCount;

        String status;
        double score;

        if (moduleMissingCount == 0) {
            status = "PASS";
            score = 100;
        } else if (missingRatio < 0.2) {
            status = "WARN";
            score = 75;
        } else {
            status = "FAIL";
            score = 50;
        }

        addItem(
                evaluateTaskId,
                "MODULE",
                "模块覆盖率",
                "缺失=" + moduleMissingCount,
                status,
                score,
                moduleMissingCount == 0
                        ? "所有 Chunk 都带有 moduleCode。"
                        : "部分 Chunk 缺少 moduleCode。",
                "建议在文档解析或切片阶段增加模块识别逻辑，保证按模块检索更准确。",
                Map.of(
                        "chunkCount", chunkCount,
                        "moduleMissingCount", moduleMissingCount,
                        "missingRatio", missingRatio
                )
        );
    }

    private void addVersionCoverageItem(
            String evaluateTaskId,
            boolean chunkTableExists,
            KnowledgeEvaluateRequest request
    ) {
        if (!chunkTableExists) {
            return;
        }

        if (!StringUtils.hasText(request.getVersionNo())) {
            addItem(
                    evaluateTaskId,
                    "VERSION",
                    "版本过滤",
                    "未传 versionNo",
                    "WARN",
                    70,
                    "本次评估没有指定 versionNo，无法检查版本维度覆盖。",
                    "建议评估时明确传入 versionNo，保证 RAG 能按版本检索。",
                    Map.of()
            );
            return;
        }

        if (!columnExists(CHUNK_TABLE, "version_no")) {
            addItem(
                    evaluateTaskId,
                    "VERSION",
                    "版本字段",
                    "缺失 version_no 字段",
                    "FAIL",
                    0,
                    "Chunk 表没有 version_no 字段，无法支持按版本检索。",
                    "请给 Chunk 表增加 version_no 字段，并在构建知识库时写入版本号。",
                    Map.of("table", CHUNK_TABLE)
            );
            return;
        }

        addItem(
                evaluateTaskId,
                "VERSION",
                "版本过滤",
                request.getVersionNo(),
                "PASS",
                100,
                "Chunk 表支持 version_no，可按版本检索。",
                "无需处理。",
                Map.of("versionNo", request.getVersionNo())
        );
    }

    private void addQueryHitItem(
            String evaluateTaskId,
            KnowledgeEvaluateRequest request,
            int queryHitCount
    ) {
        if (!StringUtils.hasText(request.getQueryText())) {
            addItem(
                    evaluateTaskId,
                    "QUERY_HIT",
                    "查询命中",
                    "未传 queryText",
                    "WARN",
                    70,
                    "本次评估未传 queryText，跳过查询命中检查。",
                    "建议传入一个典型业务问题，用于检查知识库是否能命中相关内容。",
                    Map.of()
            );
            return;
        }

        if (queryHitCount <= 0) {
            addItem(
                    evaluateTaskId,
                    "QUERY_HIT",
                    "查询命中",
                    "0",
                    "WARN",
                    60,
                    "queryText 未命中任何 Chunk 内容。",
                    "如果这是典型业务问题，说明切片内容、模块识别或检索策略可能需要优化。",
                    Map.of("queryText", request.getQueryText())
            );
            return;
        }

        addItem(
                evaluateTaskId,
                "QUERY_HIT",
                "查询命中",
                String.valueOf(queryHitCount),
                "PASS",
                100,
                "queryText 可以命中知识库内容。",
                "无需处理。",
                Map.of(
                        "queryText", request.getQueryText(),
                        "queryHitCount", queryHitCount
                )
        );
    }

    private void addItem(
            String evaluateTaskId,
            String dimension,
            String metricName,
            String metricValue,
            String status,
            double score,
            String problem,
            String suggestion,
            Map<String, Object> detail
    ) {
        KnowledgeEvaluateItem item = new KnowledgeEvaluateItem();

        item.setEvaluateTaskId(evaluateTaskId);
        item.setDimension(dimension);
        item.setMetricName(metricName);
        item.setMetricValue(metricValue);
        item.setStatus(status);
        item.setScore(score);
        item.setProblem(problem);
        item.setSuggestion(suggestion);
        item.setDetail(toJson(detail));
        item.setCreateTime(LocalDateTime.now());

        itemMapper.insert(item);
    }

    private List<KnowledgeEvaluateItem> listItems(String evaluateTaskId) {
        return itemMapper.selectList(
                new LambdaQueryWrapper<KnowledgeEvaluateItem>()
                        .eq(KnowledgeEvaluateItem::getEvaluateTaskId, evaluateTaskId)
                        .orderByAsc(KnowledgeEvaluateItem::getId)
        );
    }

    private double calculateTotalScore(List<KnowledgeEvaluateItem> items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }

        double total = 0;

        for (KnowledgeEvaluateItem item : items) {
            total += item.getScore() == null ? 0 : item.getScore();
        }

        return Math.round((total / items.size()) * 100.0) / 100.0;
    }

    private String firstKeyword(String queryText) {
        if (!StringUtils.hasText(queryText)) {
            return null;
        }

        String cleaned = queryText
                .replaceAll("[，。！？、,.!?;；:：\\n\\r\\t]", " ")
                .trim();

        if (!StringUtils.hasText(cleaned)) {
            return null;
        }

        String[] parts = cleaned.split("\\s+");

        for (String part : parts) {
            if (part.length() >= 2) {
                return part;
            }
        }

        return parts.length > 0 ? parts[0] : null;
    }

    private void markFailed(KnowledgeEvaluateTask task, String errorMessage) {
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

    private KnowledgeEvaluateTaskVO toTaskVO(KnowledgeEvaluateTask task) {
        KnowledgeEvaluateTaskVO vo = new KnowledgeEvaluateTaskVO();

        vo.setId(task.getId());
        vo.setEvaluateTaskId(task.getEvaluateTaskId());
        vo.setProjectId(task.getProjectId());
        vo.setVersionNo(task.getVersionNo());
        vo.setModuleCode(task.getModuleCode());
        vo.setQueryText(task.getQueryText());
        vo.setStatus(task.getStatus());
        vo.setTotalScore(task.getTotalScore());
        vo.setDocumentCount(task.getDocumentCount());
        vo.setChunkCount(task.getChunkCount());
        vo.setParentChunkCount(task.getParentChunkCount());
        vo.setChildChunkCount(task.getChildChunkCount());
        vo.setEmbeddingMissingCount(task.getEmbeddingMissingCount());
        vo.setModuleMissingCount(task.getModuleMissingCount());
        vo.setTooShortChunkCount(task.getTooShortChunkCount());
        vo.setTooLongChunkCount(task.getTooLongChunkCount());
        vo.setOrphanChildChunkCount(task.getOrphanChildChunkCount());
        vo.setSummary(task.getSummary());
        vo.setErrorMessage(task.getErrorMessage());
        vo.setCreateTime(task.getCreateTime());
        vo.setUpdateTime(task.getUpdateTime());

        return vo;
    }

    private KnowledgeEvaluateItemVO toItemVO(KnowledgeEvaluateItem item) {
        KnowledgeEvaluateItemVO vo = new KnowledgeEvaluateItemVO();

        vo.setId(item.getId());
        vo.setEvaluateTaskId(item.getEvaluateTaskId());
        vo.setDimension(item.getDimension());
        vo.setMetricName(item.getMetricName());
        vo.setMetricValue(item.getMetricValue());
        vo.setStatus(item.getStatus());
        vo.setScore(item.getScore());
        vo.setProblem(item.getProblem());
        vo.setSuggestion(item.getSuggestion());
        vo.setDetail(item.getDetail());
        vo.setCreateTime(item.getCreateTime());

        return vo;
    }

    private static class SqlBuilder {

        private final StringBuilder sql = new StringBuilder();

        private final List<Object> params = new java.util.ArrayList<>();

        private SqlBuilder append(String sqlPart, Object... values) {
            sql.append(sqlPart);

            if (values != null) {
                for (Object value : values) {
                    params.add(value);
                }
            }

            return this;
        }

        private String sql() {
            return sql.toString();
        }

        private List<Object> params() {
            return params;
        }
    }
}