package com.zy.testpilotai.rageval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.vo.RagContextVO;
import com.zy.testpilotai.knowledge.service.KnowledgeBaseService;
import com.zy.testpilotai.project.service.ProjectService;
import com.zy.testpilotai.rageval.mapper.RagEvalDatasetMapper;
import com.zy.testpilotai.rageval.mapper.RagEvalQuestionMapper;
import com.zy.testpilotai.rageval.mapper.RagEvalResultMapper;
import com.zy.testpilotai.rageval.mapper.RagEvalRunMapper;
import com.zy.testpilotai.rageval.model.dto.RagEvalDatasetCreateRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalDatasetDeleteRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalDatasetQueryRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalDatasetUpdateRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalQuestionCreateRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalQuestionDeleteRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalQuestionQueryRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalQuestionUpdateRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalRunQueryRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalRunRequest;
import com.zy.testpilotai.rageval.model.entity.RagEvalDataset;
import com.zy.testpilotai.rageval.model.entity.RagEvalQuestion;
import com.zy.testpilotai.rageval.model.entity.RagEvalResult;
import com.zy.testpilotai.rageval.model.entity.RagEvalRun;
import com.zy.testpilotai.rageval.model.vo.RagEvalDatasetVO;
import com.zy.testpilotai.rageval.model.vo.RagEvalQuestionVO;
import com.zy.testpilotai.rageval.model.vo.RagEvalResultVO;
import com.zy.testpilotai.rageval.model.vo.RagEvalRunDetailVO;
import com.zy.testpilotai.rageval.model.vo.RagEvalRunVO;
import com.zy.testpilotai.rageval.service.RagEvalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

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
public class RagEvalServiceImpl implements RagEvalService {

    private final RagEvalDatasetMapper datasetMapper;

    private final RagEvalQuestionMapper questionMapper;

    private final RagEvalRunMapper runMapper;

    private final RagEvalResultMapper resultMapper;

    private final KnowledgeBaseService knowledgeBaseService;

    private final ProjectService projectService;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalDatasetVO createDataset(RagEvalDatasetCreateRequest request) {
        if (request == null || request.getProjectId() == null) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "项目 ID 不能为空"
            );
        }

        if (!StringUtils.hasText(request.getDatasetName())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "评测集名称不能为空"
            );
        }

        projectService.getById(request.getProjectId());

        RagEvalDataset dataset = new RagEvalDataset();

        dataset.setDatasetId("red_" + UUID.randomUUID().toString().replace("-", ""));
        dataset.setProjectId(request.getProjectId());
        dataset.setVersionNo(request.getVersionNo());
        dataset.setModuleCode(request.getModuleCode());
        dataset.setDatasetName(request.getDatasetName());
        dataset.setDescription(request.getDescription());
        dataset.setStatus("ACTIVE");
        dataset.setCreateTime(LocalDateTime.now());
        dataset.setUpdateTime(LocalDateTime.now());

        datasetMapper.insert(dataset);

        return toDatasetVO(dataset);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalDatasetVO updateDataset(RagEvalDatasetUpdateRequest request) {
        if (request == null || !StringUtils.hasText(request.getDatasetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "评测集 ID 不能为空"
            );
        }

        RagEvalDataset dataset = getActiveDataset(request.getDatasetId());

        if (request.getVersionNo() != null) {
            dataset.setVersionNo(request.getVersionNo());
        }

        if (request.getModuleCode() != null) {
            dataset.setModuleCode(request.getModuleCode());
        }

        if (StringUtils.hasText(request.getDatasetName())) {
            dataset.setDatasetName(request.getDatasetName());
        }

        if (request.getDescription() != null) {
            dataset.setDescription(request.getDescription());
        }

        dataset.setUpdateTime(LocalDateTime.now());

        datasetMapper.updateById(dataset);

        return toDatasetVO(dataset);
    }

    @Override
    public List<RagEvalDatasetVO> listDatasets(RagEvalDatasetQueryRequest request) {
        LambdaQueryWrapper<RagEvalDataset> wrapper =
                new LambdaQueryWrapper<RagEvalDataset>()
                        .orderByDesc(RagEvalDataset::getCreateTime)
                        .orderByDesc(RagEvalDataset::getId);

        if (request == null || !StringUtils.hasText(request.getStatus())) {
            wrapper.eq(RagEvalDataset::getStatus, "ACTIVE");
        } else {
            wrapper.eq(RagEvalDataset::getStatus, request.getStatus());
        }

        if (request != null && request.getProjectId() != null) {
            wrapper.eq(RagEvalDataset::getProjectId, request.getProjectId());
        }

        if (request != null && StringUtils.hasText(request.getVersionNo())) {
            wrapper.eq(RagEvalDataset::getVersionNo, request.getVersionNo());
        }

        if (request != null && StringUtils.hasText(request.getModuleCode())) {
            wrapper.eq(RagEvalDataset::getModuleCode, request.getModuleCode());
        }

        if (request != null && StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(RagEvalDataset::getDatasetName, request.getKeyword())
                    .or()
                    .like(RagEvalDataset::getDescription, request.getKeyword())
            );
        }

        return datasetMapper.selectList(wrapper)
                .stream()
                .map(this::toDatasetVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDataset(RagEvalDatasetDeleteRequest request) {
        if (request == null || !StringUtils.hasText(request.getDatasetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "评测集 ID 不能为空"
            );
        }

        RagEvalDataset dataset = getActiveDataset(request.getDatasetId());

        dataset.setStatus("DELETED");
        dataset.setUpdateTime(LocalDateTime.now());

        datasetMapper.updateById(dataset);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalQuestionVO createQuestion(RagEvalQuestionCreateRequest request) {
        if (request == null || !StringUtils.hasText(request.getDatasetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "评测集 ID 不能为空"
            );
        }

        if (!StringUtils.hasText(request.getQuestionText())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "问题文本不能为空"
            );
        }

        getActiveDataset(request.getDatasetId());

        RagEvalQuestion question = new RagEvalQuestion();

        question.setQuestionId("req_" + UUID.randomUUID().toString().replace("-", ""));
        question.setDatasetId(request.getDatasetId());
        question.setQuestionText(request.getQuestionText());
        question.setStandardAnswer(request.getStandardAnswer());
        question.setExpectedKeywords(toJson(defaultList(request.getExpectedKeywords())));
        question.setExpectedChunkIds(toJson(defaultList(request.getExpectedChunkIds())));
        question.setExpectedDocumentIds(toJson(defaultList(request.getExpectedDocumentIds())));
        question.setExpectedModuleCode(request.getExpectedModuleCode());
        question.setExpectedVersionNo(request.getExpectedVersionNo());
        question.setDifficulty(normalizeDifficulty(request.getDifficulty()));
        question.setStatus("ACTIVE");
        question.setCreateTime(LocalDateTime.now());
        question.setUpdateTime(LocalDateTime.now());

        questionMapper.insert(question);

        return toQuestionVO(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalQuestionVO updateQuestion(RagEvalQuestionUpdateRequest request) {
        if (request == null || !StringUtils.hasText(request.getQuestionId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "问题 ID 不能为空"
            );
        }

        RagEvalQuestion question = getActiveQuestion(request.getQuestionId());

        if (StringUtils.hasText(request.getQuestionText())) {
            question.setQuestionText(request.getQuestionText());
        }

        if (request.getStandardAnswer() != null) {
            question.setStandardAnswer(request.getStandardAnswer());
        }

        if (request.getExpectedKeywords() != null) {
            question.setExpectedKeywords(toJson(defaultList(request.getExpectedKeywords())));
        }

        if (request.getExpectedChunkIds() != null) {
            question.setExpectedChunkIds(toJson(defaultList(request.getExpectedChunkIds())));
        }

        if (request.getExpectedDocumentIds() != null) {
            question.setExpectedDocumentIds(toJson(defaultList(request.getExpectedDocumentIds())));
        }

        if (request.getExpectedModuleCode() != null) {
            question.setExpectedModuleCode(request.getExpectedModuleCode());
        }

        if (request.getExpectedVersionNo() != null) {
            question.setExpectedVersionNo(request.getExpectedVersionNo());
        }

        if (StringUtils.hasText(request.getDifficulty())) {
            question.setDifficulty(normalizeDifficulty(request.getDifficulty()));
        }

        question.setUpdateTime(LocalDateTime.now());

        questionMapper.updateById(question);

        return toQuestionVO(question);
    }

    @Override
    public List<RagEvalQuestionVO> listQuestions(RagEvalQuestionQueryRequest request) {
        LambdaQueryWrapper<RagEvalQuestion> wrapper =
                new LambdaQueryWrapper<RagEvalQuestion>()
                        .orderByAsc(RagEvalQuestion::getId);

        if (request == null || !StringUtils.hasText(request.getStatus())) {
            wrapper.eq(RagEvalQuestion::getStatus, "ACTIVE");
        } else {
            wrapper.eq(RagEvalQuestion::getStatus, request.getStatus());
        }

        if (request != null && StringUtils.hasText(request.getDatasetId())) {
            wrapper.eq(RagEvalQuestion::getDatasetId, request.getDatasetId());
        }

        if (request != null && StringUtils.hasText(request.getDifficulty())) {
            wrapper.eq(RagEvalQuestion::getDifficulty, normalizeDifficulty(request.getDifficulty()));
        }

        if (request != null && StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(RagEvalQuestion::getQuestionText, request.getKeyword())
                    .or()
                    .like(RagEvalQuestion::getStandardAnswer, request.getKeyword())
            );
        }

        return questionMapper.selectList(wrapper)
                .stream()
                .map(this::toQuestionVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteQuestion(RagEvalQuestionDeleteRequest request) {
        if (request == null || !StringUtils.hasText(request.getQuestionId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "问题 ID 不能为空"
            );
        }

        RagEvalQuestion question = getActiveQuestion(request.getQuestionId());

        question.setStatus("DELETED");
        question.setUpdateTime(LocalDateTime.now());

        questionMapper.updateById(question);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RagEvalRunDetailVO run(RagEvalRunRequest request) {
        if (request == null || !StringUtils.hasText(request.getDatasetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "评测集 ID 不能为空"
            );
        }

        RagEvalDataset dataset = getActiveDataset(request.getDatasetId());

        List<RagEvalQuestion> questions = questionMapper.selectList(
                new LambdaQueryWrapper<RagEvalQuestion>()
                        .eq(RagEvalQuestion::getDatasetId, dataset.getDatasetId())
                        .eq(RagEvalQuestion::getStatus, "ACTIVE")
                        .orderByAsc(RagEvalQuestion::getId)
        );

        if (questions.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "评测集下没有有效问题，无法运行评测"
            );
        }

        int topK = request.getTopK() == null || request.getTopK() <= 0
                ? 5
                : request.getTopK();

        String runVersionNo = StringUtils.hasText(request.getVersionNo())
                ? request.getVersionNo()
                : dataset.getVersionNo();

        String runModuleCode = StringUtils.hasText(request.getModuleCode())
                ? request.getModuleCode()
                : dataset.getModuleCode();

        String runId = "rer_" + UUID.randomUUID().toString().replace("-", "");

        RagEvalRun run = createRun(runId, dataset, runVersionNo, runModuleCode, topK);

        try {
            int hitCount = 0;
            int sourceHitCount = 0;
            double reciprocalRankTotal = 0;
            double scoreTotal = 0;

            for (RagEvalQuestion question : questions) {
                EvalOneResult evalOneResult = evaluateOneQuestion(
                        run,
                        question,
                        topK
                );

                if (evalOneResult.hit()) {
                    hitCount++;
                }

                if (evalOneResult.sourceHit()) {
                    sourceHitCount++;
                }

                if (evalOneResult.hitRank() > 0) {
                    reciprocalRankTotal += 1.0 / evalOneResult.hitRank();
                }

                scoreTotal += evalOneResult.score();
            }

            int total = questions.size();

            double recallAtK = round(hitCount * 1.0 / total);
            double mrr = round(reciprocalRankTotal / total);
            double sourceHitRate = round(sourceHitCount * 1.0 / total);
            double avgScore = round(scoreTotal / total);

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("runId", runId);
            summary.put("datasetId", dataset.getDatasetId());
            summary.put("datasetName", dataset.getDatasetName());
            summary.put("projectId", dataset.getProjectId());
            summary.put("versionNo", runVersionNo);
            summary.put("moduleCode", runModuleCode);
            summary.put("topK", topK);
            summary.put("totalQuestions", total);
            summary.put("hitCount", hitCount);
            summary.put("recallAtK", recallAtK);
            summary.put("mrr", mrr);
            summary.put("sourceHitRate", sourceHitRate);
            summary.put("avgScore", avgScore);

            run.setStatus("SUCCESS");
            run.setTotalQuestions(total);
            run.setHitCount(hitCount);
            run.setRecallAtK(recallAtK);
            run.setMrr(mrr);
            run.setSourceHitRate(sourceHitRate);
            run.setAvgScore(avgScore);
            run.setSummary(toJson(summary));
            run.setUpdateTime(LocalDateTime.now());

            runMapper.updateById(run);

            return runDetail(runId);
        } catch (BusinessException e) {
            markRunFailed(run, e.getMessage());
            throw e;
        } catch (Exception e) {
            markRunFailed(run, e.getMessage());
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "RAG 评测运行失败：" + e.getMessage()
            );
        }
    }

    @Override
    public List<RagEvalRunVO> listRuns(RagEvalRunQueryRequest request) {
        LambdaQueryWrapper<RagEvalRun> wrapper =
                new LambdaQueryWrapper<RagEvalRun>()
                        .orderByDesc(RagEvalRun::getCreateTime)
                        .orderByDesc(RagEvalRun::getId);

        if (request != null && StringUtils.hasText(request.getDatasetId())) {
            wrapper.eq(RagEvalRun::getDatasetId, request.getDatasetId());
        }

        if (request != null && request.getProjectId() != null) {
            wrapper.eq(RagEvalRun::getProjectId, request.getProjectId());
        }

        if (request != null && StringUtils.hasText(request.getVersionNo())) {
            wrapper.eq(RagEvalRun::getVersionNo, request.getVersionNo());
        }

        if (request != null && StringUtils.hasText(request.getModuleCode())) {
            wrapper.eq(RagEvalRun::getModuleCode, request.getModuleCode());
        }

        if (request != null && StringUtils.hasText(request.getStatus())) {
            wrapper.eq(RagEvalRun::getStatus, request.getStatus());
        }

        return runMapper.selectList(wrapper)
                .stream()
                .map(this::toRunVO)
                .toList();
    }

    @Override
    public RagEvalRunDetailVO runDetail(String runId) {
        if (!StringUtils.hasText(runId)) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "运行任务 ID 不能为空"
            );
        }

        RagEvalRun run = runMapper.selectOne(
                new LambdaQueryWrapper<RagEvalRun>()
                        .eq(RagEvalRun::getRunId, runId)
                        .last("LIMIT 1")
        );

        if (run == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "RAG 评测运行任务不存在"
            );
        }

        List<RagEvalResultVO> results = resultMapper.selectList(
                        new LambdaQueryWrapper<RagEvalResult>()
                                .eq(RagEvalResult::getRunId, runId)
                                .orderByAsc(RagEvalResult::getId)
                )
                .stream()
                .map(this::toResultVO)
                .toList();

        RagEvalRunDetailVO detailVO = new RagEvalRunDetailVO();
        detailVO.setRun(toRunVO(run));
        detailVO.setResults(results);

        return detailVO;
    }

    private RagEvalRun createRun(
            String runId,
            RagEvalDataset dataset,
            String versionNo,
            String moduleCode,
            int topK
    ) {
        RagEvalRun run = new RagEvalRun();

        run.setRunId(runId);
        run.setDatasetId(dataset.getDatasetId());
        run.setProjectId(dataset.getProjectId());
        run.setVersionNo(versionNo);
        run.setModuleCode(moduleCode);
        run.setTopK(topK);
        run.setStatus("RUNNING");
        run.setTotalQuestions(0);
        run.setHitCount(0);
        run.setRecallAtK(0.0);
        run.setMrr(0.0);
        run.setSourceHitRate(0.0);
        run.setAvgScore(0.0);
        run.setCreateTime(LocalDateTime.now());
        run.setUpdateTime(LocalDateTime.now());

        runMapper.insert(run);

        return run;
    }

    private EvalOneResult evaluateOneQuestion(
            RagEvalRun run,
            RagEvalQuestion question,
            int topK
    ) {
        KnowledgeSearchRequest searchRequest = new KnowledgeSearchRequest();
        searchRequest.setProjectId(run.getProjectId());
        searchRequest.setVersionNo(run.getVersionNo());
        searchRequest.setModuleCode(run.getModuleCode());
        searchRequest.setTopK(topK);
        searchRequest.setQuery(question.getQuestionText());

        RagContextVO ragContext = knowledgeBaseService.buildRagContext(searchRequest);

        JsonNode contextNode = objectMapper.valueToTree(ragContext);
        List<String> contextItems = extractContextItems(contextNode);

        List<String> expectedKeywords = parseStringList(question.getExpectedKeywords());
        List<String> expectedChunkIds = parseStringList(question.getExpectedChunkIds());
        List<String> expectedDocumentIds = parseStringList(question.getExpectedDocumentIds());

        HitInfo keywordHitInfo = evaluateKeywordHit(contextItems, expectedKeywords);
        HitInfo sourceHitInfo = evaluateSourceHit(contextItems, expectedChunkIds, expectedDocumentIds);
        HitInfo moduleHitInfo = evaluateTextHit(contextItems, question.getExpectedModuleCode());
        HitInfo versionHitInfo = evaluateTextHit(contextItems, question.getExpectedVersionNo());

        boolean hasSourceExpectation =
                !CollectionUtils.isEmpty(expectedChunkIds)
                        || !CollectionUtils.isEmpty(expectedDocumentIds);

        boolean hasKeywordExpectation = !CollectionUtils.isEmpty(expectedKeywords);

        boolean hasModuleExpectation = StringUtils.hasText(question.getExpectedModuleCode());

        boolean hasVersionExpectation = StringUtils.hasText(question.getExpectedVersionNo());

        boolean hit;

        if (hasSourceExpectation) {
            hit = sourceHitInfo.hit();
        } else if (hasKeywordExpectation) {
            hit = keywordHitInfo.hit();
        } else if (hasModuleExpectation || hasVersionExpectation) {
            hit = (!hasModuleExpectation || moduleHitInfo.hit())
                    && (!hasVersionExpectation || versionHitInfo.hit());
        } else {
            hit = !contextItems.isEmpty();
        }

        int hitRank = firstPositiveRank(
                sourceHitInfo.rank(),
                keywordHitInfo.rank(),
                moduleHitInfo.rank(),
                versionHitInfo.rank()
        );

        boolean sourceHit = sourceHitInfo.hit();

        double score = calculateQuestionScore(
                hit,
                keywordHitInfo.hit(),
                sourceHitInfo.hit(),
                moduleHitInfo.hit(),
                versionHitInfo.hit(),
                hasKeywordExpectation,
                hasSourceExpectation,
                hasModuleExpectation,
                hasVersionExpectation
        );

        String evaluationMessage = buildEvaluationMessage(
                hit,
                sourceHit,
                keywordHitInfo,
                moduleHitInfo,
                versionHitInfo
        );

        RagEvalResult result = new RagEvalResult();

        result.setRunId(run.getRunId());
        result.setQuestionId(question.getQuestionId());
        result.setQuestionText(question.getQuestionText());
        result.setStandardAnswer(question.getStandardAnswer());
        result.setExpectedKeywords(question.getExpectedKeywords());
        result.setRetrievedContext(toJson(contextNode));
        result.setHit(hit ? 1 : 0);
        result.setHitRank(hitRank);
        result.setSourceHit(sourceHit ? 1 : 0);
        result.setMatchedKeywords(toJson(keywordHitInfo.matchedValues()));
        result.setScore(score);
        result.setEvaluationMessage(evaluationMessage);
        result.setCreateTime(LocalDateTime.now());

        resultMapper.insert(result);

        return new EvalOneResult(
                hit,
                hitRank,
                sourceHit,
                score
        );
    }

    private HitInfo evaluateKeywordHit(
            List<String> contextItems,
            List<String> expectedKeywords
    ) {
        if (CollectionUtils.isEmpty(expectedKeywords)) {
            return new HitInfo(false, 0, List.of());
        }

        List<String> matched = new ArrayList<>();
        int firstRank = 0;

        for (int i = 0; i < contextItems.size(); i++) {
            String context = normalize(contextItems.get(i));

            for (String keyword : expectedKeywords) {
                if (!StringUtils.hasText(keyword)) {
                    continue;
                }

                String normalizedKeyword = normalize(keyword);

                if (context.contains(normalizedKeyword)) {
                    matched.add(keyword);

                    if (firstRank == 0) {
                        firstRank = i + 1;
                    }
                }
            }
        }

        return new HitInfo(
                !matched.isEmpty(),
                firstRank,
                distinct(matched)
        );
    }

    private HitInfo evaluateSourceHit(
            List<String> contextItems,
            List<String> expectedChunkIds,
            List<String> expectedDocumentIds
    ) {
        List<String> expectedValues = new ArrayList<>();
        expectedValues.addAll(defaultList(expectedChunkIds));
        expectedValues.addAll(defaultList(expectedDocumentIds));

        if (expectedValues.isEmpty()) {
            return new HitInfo(false, 0, List.of());
        }

        List<String> matched = new ArrayList<>();
        int firstRank = 0;

        for (int i = 0; i < contextItems.size(); i++) {
            String context = normalize(contextItems.get(i));

            for (String expectedValue : expectedValues) {
                if (!StringUtils.hasText(expectedValue)) {
                    continue;
                }

                String normalizedExpected = normalize(expectedValue);

                if (context.contains(normalizedExpected)) {
                    matched.add(expectedValue);

                    if (firstRank == 0) {
                        firstRank = i + 1;
                    }
                }
            }
        }

        return new HitInfo(
                !matched.isEmpty(),
                firstRank,
                distinct(matched)
        );
    }

    private HitInfo evaluateTextHit(
            List<String> contextItems,
            String expectedText
    ) {
        if (!StringUtils.hasText(expectedText)) {
            return new HitInfo(false, 0, List.of());
        }

        String normalizedExpected = normalize(expectedText);

        for (int i = 0; i < contextItems.size(); i++) {
            String context = normalize(contextItems.get(i));

            if (context.contains(normalizedExpected)) {
                return new HitInfo(
                        true,
                        i + 1,
                        List.of(expectedText)
                );
            }
        }

        return new HitInfo(false, 0, List.of());
    }

    private List<String> extractContextItems(JsonNode root) {
        List<JsonNode> candidateItems = new ArrayList<>();

        collectCandidateItems(root, candidateItems);

        if (candidateItems.isEmpty()) {
            candidateItems.add(root);
        }

        List<String> result = new ArrayList<>();

        for (JsonNode item : candidateItems) {
            result.add(item.toString());
        }

        return result;
    }

    private void collectCandidateItems(JsonNode node, List<JsonNode> result) {
        if (node == null || node.isNull() || node.isMissingNode()) {
            return;
        }

        if (node.isArray()) {
            for (JsonNode item : node) {
                if (looksLikeContextItem(item)) {
                    result.add(item);
                } else {
                    collectCandidateItems(item, result);
                }
            }
            return;
        }

        if (node.isObject()) {
            if (looksLikeContextItem(node)) {
                result.add(node);
                return;
            }

            node.fields().forEachRemaining(entry -> collectCandidateItems(entry.getValue(), result));
        }
    }

    private boolean looksLikeContextItem(JsonNode node) {
        if (node == null || !node.isObject()) {
            return false;
        }

        return hasAnyField(
                node,
                "content",
                "chunkContent",
                "text",
                "title",
                "sectionTitle",
                "chunkId",
                "documentId",
                "moduleCode",
                "versionNo"
        );
    }

    private boolean hasAnyField(JsonNode node, String... fieldNames) {
        for (String fieldName : fieldNames) {
            if (node.has(fieldName)) {
                return true;
            }
        }

        return false;
    }

    private double calculateQuestionScore(
            boolean hit,
            boolean keywordHit,
            boolean sourceHit,
            boolean moduleHit,
            boolean versionHit,
            boolean hasKeywordExpectation,
            boolean hasSourceExpectation,
            boolean hasModuleExpectation,
            boolean hasVersionExpectation
    ) {
        if (!hit) {
            return 0;
        }

        double score = 60;

        if (hasKeywordExpectation && keywordHit) {
            score += 20;
        }

        if (hasSourceExpectation && sourceHit) {
            score += 20;
        }

        if (hasModuleExpectation && moduleHit) {
            score += 10;
        }

        if (hasVersionExpectation && versionHit) {
            score += 10;
        }

        return Math.min(100, score);
    }

    private String buildEvaluationMessage(
            boolean hit,
            boolean sourceHit,
            HitInfo keywordHitInfo,
            HitInfo moduleHitInfo,
            HitInfo versionHitInfo
    ) {
        if (!hit) {
            return "未命中期望内容，请检查切片、向量化、版本过滤、模块过滤或检索策略。";
        }

        List<String> messages = new ArrayList<>();
        messages.add("已命中期望内容");

        if (!keywordHitInfo.matchedValues().isEmpty()) {
            messages.add("命中关键词：" + keywordHitInfo.matchedValues());
        }

        if (sourceHit) {
            messages.add("命中期望来源");
        }

        if (moduleHitInfo.hit()) {
            messages.add("命中期望模块");
        }

        if (versionHitInfo.hit()) {
            messages.add("命中期望版本");
        }

        return String.join("；", messages);
    }

    private int firstPositiveRank(int... ranks) {
        int result = 0;

        for (int rank : ranks) {
            if (rank <= 0) {
                continue;
            }

            if (result == 0 || rank < result) {
                result = rank;
            }
        }

        return result;
    }

    private RagEvalDataset getActiveDataset(String datasetId) {
        RagEvalDataset dataset = datasetMapper.selectOne(
                new LambdaQueryWrapper<RagEvalDataset>()
                        .eq(RagEvalDataset::getDatasetId, datasetId)
                        .last("LIMIT 1")
        );

        if (dataset == null || "DELETED".equals(dataset.getStatus())) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "RAG 评测集不存在或已删除"
            );
        }

        return dataset;
    }

    private RagEvalQuestion getActiveQuestion(String questionId) {
        RagEvalQuestion question = questionMapper.selectOne(
                new LambdaQueryWrapper<RagEvalQuestion>()
                        .eq(RagEvalQuestion::getQuestionId, questionId)
                        .last("LIMIT 1")
        );

        if (question == null || "DELETED".equals(question.getStatus())) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "RAG 评测问题不存在或已删除"
            );
        }

        return question;
    }

    private void markRunFailed(RagEvalRun run, String errorMessage) {
        run.setStatus("FAILED");
        run.setErrorMessage(errorMessage);
        run.setUpdateTime(LocalDateTime.now());

        runMapper.updateById(run);
    }

    private String normalizeDifficulty(String difficulty) {
        if (!StringUtils.hasText(difficulty)) {
            return "MEDIUM";
        }

        String value = difficulty.trim().toUpperCase();

        return switch (value) {
            case "EASY", "MEDIUM", "HARD" -> value;
            default -> "MEDIUM";
        };
    }

    private String normalize(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value
                .toLowerCase()
                .replaceAll("\\s+", "")
                .replaceAll("[\"'`，。！？、,.!?;；:：\\[\\]{}()（）]", "");
    }

    private List<String> parseStringList(String json) {
        if (!StringUtils.hasText(json) || "null".equals(json)) {
            return List.of();
        }

        try {
            return objectMapper.readValue(
                    json,
                    new TypeReference<List<String>>() {
                    }
            );
        } catch (Exception e) {
            return List.of();
        }
    }

    private List<String> defaultList(List<String> values) {
        if (values == null) {
            return List.of();
        }

        return values;
    }

    private List<String> distinct(List<String> values) {
        Set<String> seen = new HashSet<>();
        List<String> result = new ArrayList<>();

        for (String value : values) {
            if (!StringUtils.hasText(value)) {
                continue;
            }

            if (seen.add(value)) {
                result.add(value);
            }
        }

        return result;
    }

    private double round(double value) {
        return Math.round(value * 10000.0) / 10000.0;
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

    private RagEvalDatasetVO toDatasetVO(RagEvalDataset dataset) {
        RagEvalDatasetVO vo = new RagEvalDatasetVO();

        vo.setId(dataset.getId());
        vo.setDatasetId(dataset.getDatasetId());
        vo.setProjectId(dataset.getProjectId());
        vo.setVersionNo(dataset.getVersionNo());
        vo.setModuleCode(dataset.getModuleCode());
        vo.setDatasetName(dataset.getDatasetName());
        vo.setDescription(dataset.getDescription());
        vo.setStatus(dataset.getStatus());
        vo.setCreateTime(dataset.getCreateTime());
        vo.setUpdateTime(dataset.getUpdateTime());

        return vo;
    }

    private RagEvalQuestionVO toQuestionVO(RagEvalQuestion question) {
        RagEvalQuestionVO vo = new RagEvalQuestionVO();

        vo.setId(question.getId());
        vo.setQuestionId(question.getQuestionId());
        vo.setDatasetId(question.getDatasetId());
        vo.setQuestionText(question.getQuestionText());
        vo.setStandardAnswer(question.getStandardAnswer());
        vo.setExpectedKeywords(question.getExpectedKeywords());
        vo.setExpectedChunkIds(question.getExpectedChunkIds());
        vo.setExpectedDocumentIds(question.getExpectedDocumentIds());
        vo.setExpectedModuleCode(question.getExpectedModuleCode());
        vo.setExpectedVersionNo(question.getExpectedVersionNo());
        vo.setDifficulty(question.getDifficulty());
        vo.setStatus(question.getStatus());
        vo.setCreateTime(question.getCreateTime());
        vo.setUpdateTime(question.getUpdateTime());

        return vo;
    }

    private RagEvalRunVO toRunVO(RagEvalRun run) {
        RagEvalRunVO vo = new RagEvalRunVO();

        vo.setId(run.getId());
        vo.setRunId(run.getRunId());
        vo.setDatasetId(run.getDatasetId());
        vo.setProjectId(run.getProjectId());
        vo.setVersionNo(run.getVersionNo());
        vo.setModuleCode(run.getModuleCode());
        vo.setTopK(run.getTopK());
        vo.setStatus(run.getStatus());
        vo.setTotalQuestions(run.getTotalQuestions());
        vo.setHitCount(run.getHitCount());
        vo.setRecallAtK(run.getRecallAtK());
        vo.setMrr(run.getMrr());
        vo.setSourceHitRate(run.getSourceHitRate());
        vo.setAvgScore(run.getAvgScore());
        vo.setSummary(run.getSummary());
        vo.setErrorMessage(run.getErrorMessage());
        vo.setCreateTime(run.getCreateTime());
        vo.setUpdateTime(run.getUpdateTime());

        return vo;
    }

    private RagEvalResultVO toResultVO(RagEvalResult result) {
        RagEvalResultVO vo = new RagEvalResultVO();

        vo.setId(result.getId());
        vo.setRunId(result.getRunId());
        vo.setQuestionId(result.getQuestionId());
        vo.setQuestionText(result.getQuestionText());
        vo.setStandardAnswer(result.getStandardAnswer());
        vo.setExpectedKeywords(result.getExpectedKeywords());
        vo.setRetrievedContext(result.getRetrievedContext());
        vo.setHit(result.getHit());
        vo.setHitRank(result.getHitRank());
        vo.setSourceHit(result.getSourceHit());
        vo.setMatchedKeywords(result.getMatchedKeywords());
        vo.setScore(result.getScore());
        vo.setEvaluationMessage(result.getEvaluationMessage());
        vo.setCreateTime(result.getCreateTime());

        return vo;
    }

    private record EvalOneResult(
            boolean hit,
            int hitRank,
            boolean sourceHit,
            double score
    ) {
    }

    private record HitInfo(
            boolean hit,
            int rank,
            List<String> matchedValues
    ) {
    }
}