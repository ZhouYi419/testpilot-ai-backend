package com.zy.testpilotai.testcase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.testcase.mapper.TestCaseMapper;
import com.zy.testpilotai.testcase.model.dto.TestCaseManualQueryRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseReviewStatusRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseUpdateRequest;
import com.zy.testpilotai.testcase.model.entity.TestCase;
import com.zy.testpilotai.testcase.model.vo.TestCaseManualVO;
import com.zy.testpilotai.testcase.service.TestCaseManualReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestCaseManualReviewServiceImpl implements TestCaseManualReviewService {

    private final TestCaseMapper testCaseMapper;

    @Override
    public List<TestCaseManualVO> list(TestCaseManualQueryRequest request) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<TestCase>()
                .ne(TestCase::getReviewStatus, "DELETED")
                .orderByDesc(TestCase::getCreateTime)
                .orderByDesc(TestCase::getId);

        if (StringUtils.hasText(request.getTaskId())) {
            wrapper.eq(TestCase::getTaskId, request.getTaskId());
        }

        if (request.getProjectId() != null) {
            wrapper.eq(TestCase::getProjectId, request.getProjectId());
        }

        if (StringUtils.hasText(request.getVersionNo())) {
            wrapper.eq(TestCase::getVersionNo, request.getVersionNo());
        }

        if (StringUtils.hasText(request.getModuleCode())) {
            wrapper.eq(TestCase::getModuleCode, request.getModuleCode());
        }

        if (StringUtils.hasText(request.getReviewStatus())) {
            wrapper.eq(TestCase::getReviewStatus, request.getReviewStatus());
        }

        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.and(w -> w
                    .like(TestCase::getCaseTitle, request.getKeyword())
                    .or()
                    .like(TestCase::getModuleName, request.getKeyword())
                    .or()
                    .like(TestCase::getExpectedResult, request.getKeyword())
            );
        }

        return testCaseMapper.selectList(wrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseManualVO update(TestCaseUpdateRequest request) {
        if (request.getId() == null) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "测试用例 ID 不能为空"
            );
        }

        TestCase testCase = getTestCase(request.getId());

        if ("DELETED".equals(testCase.getReviewStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "测试用例已删除，不能编辑"
            );
        }

        if (StringUtils.hasText(request.getModuleCode())) {
            testCase.setModuleCode(request.getModuleCode());
        }

        if (StringUtils.hasText(request.getModuleName())) {
            testCase.setModuleName(request.getModuleName());
        }

        if (StringUtils.hasText(request.getCaseTitle())) {
            testCase.setCaseTitle(request.getCaseTitle());
        }

        if (StringUtils.hasText(request.getCaseType())) {
            testCase.setCaseType(request.getCaseType());
        }

        if (StringUtils.hasText(request.getPriority())) {
            testCase.setPriority(request.getPriority());
        }

        if (request.getPrecondition() != null) {
            testCase.setPrecondition(request.getPrecondition());
        }

        if (request.getSteps() != null) {
            testCase.setSteps(request.getSteps());
        }

        if (StringUtils.hasText(request.getExpectedResult())) {
            testCase.setExpectedResult(request.getExpectedResult());
        }

        if (request.getTestData() != null) {
            testCase.setTestData(request.getTestData());
        }

        if (request.getSourceReferences() != null) {
            testCase.setSourceReferences(request.getSourceReferences());
        }

        if (request.getRiskPoint() != null) {
            testCase.setRiskPoint(request.getRiskPoint());
        }

        if (request.getAutomationSuggestion() != null) {
            testCase.setAutomationSuggestion(request.getAutomationSuggestion());
        }

        testCase.setReviewStatus("HUMAN_REVIEWED");
        testCase.setReviewer(defaultReviewer(request.getReviewer()));
        testCase.setReviewTime(LocalDateTime.now());
        testCase.setManualComment(request.getManualComment());
        testCase.setUpdateTime(LocalDateTime.now());

        testCaseMapper.updateById(testCase);

        return toVO(testCase);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean accept(TestCaseReviewStatusRequest request) {
        updateReviewStatus(
                request,
                "ACCEPTED",
                true,
                false
        );

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean reject(TestCaseReviewStatusRequest request) {
        updateReviewStatus(
                request,
                "REJECTED",
                false,
                true
        );

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean delete(TestCaseReviewStatusRequest request) {
        updateReviewStatus(
                request,
                "DELETED",
                false,
                false
        );

        return true;
    }

    private void updateReviewStatus(
            TestCaseReviewStatusRequest request,
            String targetStatus,
            boolean setAcceptedTime,
            boolean setRejectedTime
    ) {
        if (request == null || CollectionUtils.isEmpty(request.getIds())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "测试用例 ID 列表不能为空"
            );
        }

        String reviewer = defaultReviewer(request.getReviewer());
        LocalDateTime now = LocalDateTime.now();

        for (Long id : request.getIds()) {
            TestCase testCase = getTestCase(id);

            if ("DELETED".equals(testCase.getReviewStatus())
                    && !"DELETED".equals(targetStatus)) {
                throw new BusinessException(
                        ErrorCode.PARAMS_ERROR,
                        "测试用例已删除，不能修改状态，id=" + id
                );
            }

            testCase.setReviewStatus(targetStatus);
            testCase.setReviewer(reviewer);
            testCase.setReviewTime(now);
            testCase.setManualComment(request.getManualComment());
            testCase.setUpdateTime(now);

            if (setAcceptedTime) {
                testCase.setAcceptedTime(now);
            }

            if (setRejectedTime) {
                testCase.setRejectedTime(now);
            }

            testCaseMapper.updateById(testCase);
        }
    }

    private TestCase getTestCase(Long id) {
        TestCase testCase = testCaseMapper.selectById(id);

        if (testCase == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "测试用例不存在，id=" + id
            );
        }

        return testCase;
    }

    private String defaultReviewer(String reviewer) {
        if (StringUtils.hasText(reviewer)) {
            return reviewer;
        }

        return "tester";
    }

    private TestCaseManualVO toVO(TestCase testCase) {
        TestCaseManualVO vo = new TestCaseManualVO();

        vo.setId(testCase.getId());
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
        vo.setDuplicateOfCaseId(testCase.getDuplicateOfCaseId());
        vo.setDuplicateScore(testCase.getDuplicateScore());
        vo.setDuplicateReason(testCase.getDuplicateReason());
        vo.setSourceType(testCase.getSourceType());
        vo.setReviewStatus(testCase.getReviewStatus());
        vo.setReviewer(testCase.getReviewer());
        vo.setReviewTime(testCase.getReviewTime());
        vo.setManualComment(testCase.getManualComment());
        vo.setAcceptedTime(testCase.getAcceptedTime());
        vo.setRejectedTime(testCase.getRejectedTime());
        vo.setCreateTime(testCase.getCreateTime());
        vo.setUpdateTime(testCase.getUpdateTime());

        return vo;
    }
}