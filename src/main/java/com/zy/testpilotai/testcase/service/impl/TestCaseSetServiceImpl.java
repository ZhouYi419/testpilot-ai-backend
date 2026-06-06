package com.zy.testpilotai.testcase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.testcase.mapper.TestCaseMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSetItemMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSetMapper;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetAddCasesRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetBuildFromAcceptedRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetCreateRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetDeleteRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetQueryRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetRemoveCasesRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseSetUpdateRequest;
import com.zy.testpilotai.testcase.model.entity.TestCase;
import com.zy.testpilotai.testcase.model.entity.TestCaseSet;
import com.zy.testpilotai.testcase.model.entity.TestCaseSetItem;
import com.zy.testpilotai.testcase.model.vo.TestCaseSetDetailVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseSetItemVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseSetVO;
import com.zy.testpilotai.testcase.service.TestCaseSetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TestCaseSetServiceImpl implements TestCaseSetService {

    private final TestCaseSetMapper testCaseSetMapper;

    private final TestCaseSetItemMapper testCaseSetItemMapper;

    private final TestCaseMapper testCaseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseSetVO create(TestCaseSetCreateRequest request) {
        validateCreateRequest(request);

        TestCaseSet caseSet = new TestCaseSet();

        caseSet.setCaseSetId("cs_" + UUID.randomUUID().toString().replace("-", ""));
        caseSet.setProjectId(request.getProjectId());
        caseSet.setVersionNo(request.getVersionNo());
        caseSet.setModuleCode(request.getModuleCode());
        caseSet.setSetName(request.getSetName());
        caseSet.setSetType(normalizeSetType(request.getSetType()));
        caseSet.setDescription(request.getDescription());
        caseSet.setCaseCount(0);
        caseSet.setStatus("ACTIVE");
        caseSet.setCreateTime(LocalDateTime.now());
        caseSet.setUpdateTime(LocalDateTime.now());

        testCaseSetMapper.insert(caseSet);

        return toSetVO(caseSet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseSetVO update(TestCaseSetUpdateRequest request) {
        if (!StringUtils.hasText(request.getCaseSetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "用例集 ID 不能为空"
            );
        }

        TestCaseSet caseSet = getActiveCaseSet(request.getCaseSetId());

        if (StringUtils.hasText(request.getVersionNo())) {
            caseSet.setVersionNo(request.getVersionNo());
        }

        if (request.getModuleCode() != null) {
            caseSet.setModuleCode(request.getModuleCode());
        }

        if (StringUtils.hasText(request.getSetName())) {
            caseSet.setSetName(request.getSetName());
        }

        if (StringUtils.hasText(request.getSetType())) {
            caseSet.setSetType(normalizeSetType(request.getSetType()));
        }

        if (request.getDescription() != null) {
            caseSet.setDescription(request.getDescription());
        }

        caseSet.setUpdateTime(LocalDateTime.now());

        testCaseSetMapper.updateById(caseSet);

        return toSetVO(caseSet);
    }

    @Override
    public List<TestCaseSetVO> list(TestCaseSetQueryRequest request) {
        LambdaQueryWrapper<TestCaseSet> wrapper = new LambdaQueryWrapper<TestCaseSet>()
                .orderByDesc(TestCaseSet::getCreateTime)
                .orderByDesc(TestCaseSet::getId);

        if (request == null || !StringUtils.hasText(request.getStatus())) {
            wrapper.eq(TestCaseSet::getStatus, "ACTIVE");
        } else {
            wrapper.eq(TestCaseSet::getStatus, request.getStatus());
        }

        if (request != null && request.getProjectId() != null) {
            wrapper.eq(TestCaseSet::getProjectId, request.getProjectId());
        }

        if (request != null && StringUtils.hasText(request.getVersionNo())) {
            wrapper.eq(TestCaseSet::getVersionNo, request.getVersionNo());
        }

        if (request != null && StringUtils.hasText(request.getModuleCode())) {
            wrapper.eq(TestCaseSet::getModuleCode, request.getModuleCode());
        }

        if (request != null && StringUtils.hasText(request.getSetType())) {
            wrapper.eq(TestCaseSet::getSetType, normalizeSetType(request.getSetType()));
        }

        if (request != null && StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(TestCaseSet::getSetName, request.getKeyword())
                    .or()
                    .like(TestCaseSet::getDescription, request.getKeyword())
            );
        }

        return testCaseSetMapper.selectList(wrapper)
                .stream()
                .map(this::toSetVO)
                .toList();
    }

    @Override
    public TestCaseSetDetailVO detail(String caseSetId) {
        TestCaseSet caseSet = getActiveCaseSet(caseSetId);

        List<TestCaseSetItem> items = testCaseSetItemMapper.selectList(
                new LambdaQueryWrapper<TestCaseSetItem>()
                        .eq(TestCaseSetItem::getCaseSetId, caseSetId)
                        .orderByAsc(TestCaseSetItem::getItemOrder)
                        .orderByAsc(TestCaseSetItem::getId)
        );

        List<TestCaseSetItemVO> itemVOList = items.stream()
                .map(this::toItemVO)
                .toList();

        TestCaseSetDetailVO detailVO = new TestCaseSetDetailVO();
        detailVO.setCaseSet(toSetVO(caseSet));
        detailVO.setCases(itemVOList);

        return detailVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseSetDetailVO addCases(TestCaseSetAddCasesRequest request) {
        if (!StringUtils.hasText(request.getCaseSetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "用例集 ID 不能为空"
            );
        }

        if (CollectionUtils.isEmpty(request.getTestCaseIds())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "测试用例 ID 列表不能为空"
            );
        }

        TestCaseSet caseSet = getActiveCaseSet(request.getCaseSetId());

        int nextOrder = maxItemOrder(caseSet.getCaseSetId()) + 1;

        for (Long testCaseId : request.getTestCaseIds()) {
            TestCase testCase = getValidAcceptedTestCase(testCaseId);

            boolean exists = testCaseSetItemMapper.selectCount(
                    new LambdaQueryWrapper<TestCaseSetItem>()
                            .eq(TestCaseSetItem::getCaseSetId, caseSet.getCaseSetId())
                            .eq(TestCaseSetItem::getTestCaseId, testCase.getId())
            ) > 0;

            if (exists) {
                continue;
            }

            TestCaseSetItem item = new TestCaseSetItem();
            item.setCaseSetId(caseSet.getCaseSetId());
            item.setTestCaseId(testCase.getId());
            item.setItemOrder(nextOrder++);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());

            testCaseSetItemMapper.insert(item);
        }

        refreshCaseCount(caseSet.getCaseSetId());

        return detail(caseSet.getCaseSetId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseSetDetailVO removeCases(TestCaseSetRemoveCasesRequest request) {
        if (!StringUtils.hasText(request.getCaseSetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "用例集 ID 不能为空"
            );
        }

        if (CollectionUtils.isEmpty(request.getTestCaseIds())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "测试用例 ID 列表不能为空"
            );
        }

        TestCaseSet caseSet = getActiveCaseSet(request.getCaseSetId());

        testCaseSetItemMapper.delete(
                new LambdaQueryWrapper<TestCaseSetItem>()
                        .eq(TestCaseSetItem::getCaseSetId, caseSet.getCaseSetId())
                        .in(TestCaseSetItem::getTestCaseId, request.getTestCaseIds())
        );

        refreshCaseCount(caseSet.getCaseSetId());

        return detail(caseSet.getCaseSetId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseSetDetailVO buildFromAccepted(TestCaseSetBuildFromAcceptedRequest request) {
        if (request.getProjectId() == null) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "项目 ID 不能为空"
            );
        }

        if (!StringUtils.hasText(request.getVersionNo())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "版本号不能为空"
            );
        }

        if (!StringUtils.hasText(request.getSetName())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "用例集名称不能为空"
            );
        }

        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<TestCase>()
                .eq(TestCase::getProjectId, request.getProjectId())
                .eq(TestCase::getVersionNo, request.getVersionNo())
                .eq(TestCase::getReviewStatus, "ACCEPTED")
                .orderByAsc(TestCase::getId);

        if (StringUtils.hasText(request.getModuleCode())) {
            wrapper.eq(TestCase::getModuleCode, request.getModuleCode());
        }

        if (StringUtils.hasText(request.getTaskId())) {
            wrapper.eq(TestCase::getTaskId, request.getTaskId());
        }

        List<TestCase> acceptedCases = testCaseMapper.selectList(wrapper);

        if (acceptedCases.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "没有找到已采纳的测试用例，无法构建用例集"
            );
        }

        TestCaseSetCreateRequest createRequest = new TestCaseSetCreateRequest();
        createRequest.setProjectId(request.getProjectId());
        createRequest.setVersionNo(request.getVersionNo());
        createRequest.setModuleCode(request.getModuleCode());
        createRequest.setSetName(request.getSetName());
        createRequest.setSetType(request.getSetType());
        createRequest.setDescription(request.getDescription());

        TestCaseSetVO createdSet = create(createRequest);

        TestCaseSetAddCasesRequest addCasesRequest = new TestCaseSetAddCasesRequest();
        addCasesRequest.setCaseSetId(createdSet.getCaseSetId());
        addCasesRequest.setTestCaseIds(
                acceptedCases.stream()
                        .map(TestCase::getId)
                        .toList()
        );

        return addCases(addCasesRequest);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(TestCaseSetDeleteRequest request) {
        if (request == null || !StringUtils.hasText(request.getCaseSetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "用例集 ID 不能为空"
            );
        }

        TestCaseSet caseSet = getActiveCaseSet(request.getCaseSetId());

        caseSet.setStatus("DELETED");
        caseSet.setUpdateTime(LocalDateTime.now());

        testCaseSetMapper.updateById(caseSet);

        return true;
    }

    private void validateCreateRequest(TestCaseSetCreateRequest request) {
        if (request.getProjectId() == null) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "项目 ID 不能为空"
            );
        }

        if (!StringUtils.hasText(request.getSetName())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "用例集名称不能为空"
            );
        }
    }

    private TestCaseSet getActiveCaseSet(String caseSetId) {
        if (!StringUtils.hasText(caseSetId)) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "用例集 ID 不能为空"
            );
        }

        TestCaseSet caseSet = testCaseSetMapper.selectOne(
                new LambdaQueryWrapper<TestCaseSet>()
                        .eq(TestCaseSet::getCaseSetId, caseSetId)
                        .last("LIMIT 1")
        );

        if (caseSet == null || "DELETED".equals(caseSet.getStatus())) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "用例集不存在或已删除"
            );
        }

        return caseSet;
    }

    private TestCase getValidAcceptedTestCase(Long testCaseId) {
        if (testCaseId == null) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "测试用例 ID 不能为空"
            );
        }

        TestCase testCase = testCaseMapper.selectById(testCaseId);

        if (testCase == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "测试用例不存在，id=" + testCaseId
            );
        }

        if ("DELETED".equals(testCase.getReviewStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "测试用例已删除，不能加入用例集，id=" + testCaseId
            );
        }

        if (!"ACCEPTED".equals(testCase.getReviewStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "只有已采纳的测试用例才能加入正式用例集，id=" + testCaseId
            );
        }

        return testCase;
    }

    private int maxItemOrder(String caseSetId) {
        return testCaseSetItemMapper.selectList(
                        new LambdaQueryWrapper<TestCaseSetItem>()
                                .eq(TestCaseSetItem::getCaseSetId, caseSetId)
                )
                .stream()
                .map(TestCaseSetItem::getItemOrder)
                .filter(order -> order != null)
                .max(Integer::compareTo)
                .orElse(0);
    }

    private void refreshCaseCount(String caseSetId) {
        TestCaseSet caseSet = getActiveCaseSet(caseSetId);

        Long count = testCaseSetItemMapper.selectCount(
                new LambdaQueryWrapper<TestCaseSetItem>()
                        .eq(TestCaseSetItem::getCaseSetId, caseSetId)
        );

        caseSet.setCaseCount(count == null ? 0 : count.intValue());
        caseSet.setUpdateTime(LocalDateTime.now());

        testCaseSetMapper.updateById(caseSet);
    }

    private String normalizeSetType(String setType) {
        if (!StringUtils.hasText(setType)) {
            return "CUSTOM";
        }

        String value = setType.trim().toUpperCase();

        return switch (value) {
            case "FULL", "INCREMENTAL", "REGRESSION", "AI_APP", "CUSTOM" -> value;
            default -> "CUSTOM";
        };
    }

    private TestCaseSetVO toSetVO(TestCaseSet caseSet) {
        TestCaseSetVO vo = new TestCaseSetVO();

        vo.setId(caseSet.getId());
        vo.setCaseSetId(caseSet.getCaseSetId());
        vo.setProjectId(caseSet.getProjectId());
        vo.setVersionNo(caseSet.getVersionNo());
        vo.setModuleCode(caseSet.getModuleCode());
        vo.setSetName(caseSet.getSetName());
        vo.setSetType(caseSet.getSetType());
        vo.setDescription(caseSet.getDescription());
        vo.setCaseCount(caseSet.getCaseCount());
        vo.setStatus(caseSet.getStatus());
        vo.setCreateTime(caseSet.getCreateTime());
        vo.setUpdateTime(caseSet.getUpdateTime());

        return vo;
    }

    private TestCaseSetItemVO toItemVO(TestCaseSetItem item) {
        TestCase testCase = testCaseMapper.selectById(item.getTestCaseId());

        TestCaseSetItemVO vo = new TestCaseSetItemVO();

        vo.setItemId(item.getId());
        vo.setCaseSetId(item.getCaseSetId());
        vo.setTestCaseId(item.getTestCaseId());
        vo.setItemOrder(item.getItemOrder());

        if (testCase != null) {
            vo.setTaskId(testCase.getTaskId());
            vo.setProjectId(testCase.getProjectId());
            vo.setVersionNo(testCase.getVersionNo());
            vo.setModuleCode(testCase.getModuleCode());
            vo.setModuleName(testCase.getModuleName());
            vo.setCaseTitle(testCase.getCaseTitle());
            vo.setCaseType(testCase.getCaseType());
            vo.setPriority(testCase.getPriority());
            vo.setPrecondition(testCase.getPrecondition());
            vo.setSteps(testCase.getSteps());
            vo.setExpectedResult(testCase.getExpectedResult());
            vo.setTestData(testCase.getTestData());
            vo.setSourceReferences(testCase.getSourceReferences());
            vo.setRiskPoint(testCase.getRiskPoint());
            vo.setAutomationSuggestion(testCase.getAutomationSuggestion());
            vo.setQualityScore(testCase.getQualityScore());
            vo.setDuplicateStatus(testCase.getDuplicateStatus());
            vo.setSourceType(testCase.getSourceType());
            vo.setReviewStatus(testCase.getReviewStatus());
            vo.setReviewer(testCase.getReviewer());
            vo.setManualComment(testCase.getManualComment());
            vo.setCreateTime(testCase.getCreateTime());
            vo.setUpdateTime(testCase.getUpdateTime());
        }

        return vo;
    }
}