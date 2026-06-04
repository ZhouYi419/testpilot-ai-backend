package com.zy.testpilotai.testcase.service.impl;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.common.utils.CaseSimilarityUtils;
import com.zy.testpilotai.testcase.mapper.TestCaseMapper;
import com.zy.testpilotai.testcase.model.dto.TestCaseDeduplicateRequest;
import com.zy.testpilotai.testcase.model.dto.TestCaseExportRequest;
import com.zy.testpilotai.testcase.model.entity.TestCase;
import com.zy.testpilotai.testcase.model.excel.TestCaseExcelRow;
import com.zy.testpilotai.testcase.model.vo.DuplicateCaseVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseDeduplicateResultVO;
import com.zy.testpilotai.testcase.service.TestCaseToolService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestCaseToolServiceImpl implements TestCaseToolService {

    private final TestCaseMapper testCaseMapper;

    @Override
    public TestCaseDeduplicateResultVO deduplicate(TestCaseDeduplicateRequest request) {
        double threshold = request.getThreshold() == null ? 0.85 : request.getThreshold();

        if (threshold < 0.5 || threshold > 1.0) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "相似度阈值必须在 0.5 到 1.0 之间"
            );
        }

        List<TestCase> testCases = queryTestCasesForDeduplicate(request);

        if (testCases.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "没有找到可去重的测试用例"
            );
        }

        /*
         * 每次重新去重前，先把这些用例的重复状态重置为 NORMAL。
         * 避免调整阈值后，旧状态干扰新结果。
         */
        resetDuplicateStatus(testCases);

        List<DuplicateCaseVO> duplicateCases = new ArrayList<>();

        for (int i = 0; i < testCases.size(); i++) {
            TestCase baseCase = testCases.get(i);

            if ("DUPLICATE".equals(baseCase.getDuplicateStatus())) {
                continue;
            }

            for (int j = i + 1; j < testCases.size(); j++) {
                TestCase candidateCase = testCases.get(j);

                if ("DUPLICATE".equals(candidateCase.getDuplicateStatus())) {
                    continue;
                }

                double score = calculateCaseSimilarity(baseCase, candidateCase);

                if (score >= threshold) {
                    markAsDuplicate(candidateCase, baseCase, score);

                    DuplicateCaseVO duplicateVO = new DuplicateCaseVO();
                    duplicateVO.setCaseId(candidateCase.getId());
                    duplicateVO.setCaseTitle(candidateCase.getCaseTitle());
                    duplicateVO.setDuplicateOfCaseId(baseCase.getId());
                    duplicateVO.setDuplicateOfCaseTitle(baseCase.getCaseTitle());
                    duplicateVO.setSimilarityScore(score);
                    duplicateVO.setDuplicateReason("标题、步骤和预期结果相似度达到 " + score);

                    duplicateCases.add(duplicateVO);
                }
            }
        }

        TestCaseDeduplicateResultVO resultVO = new TestCaseDeduplicateResultVO();
        resultVO.setTotalCaseCount(testCases.size());
        resultVO.setDuplicateCaseCount(duplicateCases.size());
        resultVO.setThreshold(threshold);
        resultVO.setDuplicateCases(duplicateCases);

        return resultVO;
    }

    @Override
    public void exportExcel(TestCaseExportRequest request, HttpServletResponse response) {
        List<TestCase> testCases = queryTestCasesForExport(request);

        if (testCases.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "没有可导出的测试用例"
            );
        }

        List<TestCaseExcelRow> rows = testCases.stream()
                .map(this::toExcelRow)
                .toList();

        try {
            String filename = "test_cases_" + System.currentTimeMillis() + ".xlsx";
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8)
                    .replaceAll("\\+", "%20");

            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            response.setHeader(
                    "Content-Disposition",
                    "attachment;filename*=UTF-8''" + encodedFilename
            );

            /*
             * 使用 EasyExcel 直接写入 response 输出流。
             */
            EasyExcel.write(response.getOutputStream(), TestCaseExcelRow.class)
                    .sheet("测试用例")
                    .doWrite(rows);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "导出测试用例 Excel 失败：" + e.getMessage()
            );
        }
    }

    private List<TestCase> queryTestCasesForDeduplicate(TestCaseDeduplicateRequest request) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<TestCase>()
                .orderByAsc(TestCase::getId);

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

        return testCaseMapper.selectList(wrapper);
    }

    private List<TestCase> queryTestCasesForExport(TestCaseExportRequest request) {
        LambdaQueryWrapper<TestCase> wrapper = new LambdaQueryWrapper<TestCase>()
                .orderByAsc(TestCase::getId);

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

        /*
         * 默认不导出重复用例。
         * 如果 includeDuplicate=true，则导出全部。
         */
        if (!Boolean.TRUE.equals(request.getIncludeDuplicate())) {
            wrapper.and(w -> w
                    .isNull(TestCase::getDuplicateStatus)
                    .or()
                    .eq(TestCase::getDuplicateStatus, "NORMAL")
            );
        }

        return testCaseMapper.selectList(wrapper);
    }

    private void resetDuplicateStatus(List<TestCase> testCases) {
        for (TestCase testCase : testCases) {
            testCase.setDuplicateStatus("NORMAL");
            testCase.setDuplicateOfCaseId(null);
            testCase.setDuplicateScore(null);
            testCase.setDuplicateReason(null);
            testCase.setUpdateTime(LocalDateTime.now());
            testCaseMapper.updateById(testCase);
        }
    }

    private void markAsDuplicate(TestCase duplicateCase, TestCase baseCase, double score) {
        duplicateCase.setDuplicateStatus("DUPLICATE");
        duplicateCase.setDuplicateOfCaseId(baseCase.getId());
        duplicateCase.setDuplicateScore(score);
        duplicateCase.setDuplicateReason("与用例【" + baseCase.getCaseTitle() + "】高度相似");
        duplicateCase.setUpdateTime(LocalDateTime.now());

        testCaseMapper.updateById(duplicateCase);
    }

    private double calculateCaseSimilarity(TestCase caseA, TestCase caseB) {
        String textA = buildComparableText(caseA);
        String textB = buildComparableText(caseB);

        return CaseSimilarityUtils.similarity(textA, textB);
    }

    private String buildComparableText(TestCase testCase) {
        /*
         * 用例去重不能只看标题。
         * 这里组合标题 + 步骤 + 预期结果，判断会更稳定。
         */
        return nullToEmpty(testCase.getCaseTitle())
                + "\n"
                + nullToEmpty(testCase.getSteps())
                + "\n"
                + nullToEmpty(testCase.getExpectedResult());
    }

    private TestCaseExcelRow toExcelRow(TestCase testCase) {
        TestCaseExcelRow row = new TestCaseExcelRow();

        row.setId(testCase.getId());
        row.setTaskId(testCase.getTaskId());
        row.setProjectId(testCase.getProjectId());
        row.setVersionNo(testCase.getVersionNo());
        row.setModuleCode(testCase.getModuleCode());
        row.setModuleName(testCase.getModuleName());
        row.setCaseTitle(testCase.getCaseTitle());
        row.setCaseType(testCase.getCaseType());
        row.setPriority(testCase.getPriority());
        row.setPrecondition(testCase.getPrecondition());
        row.setSteps(testCase.getSteps());
        row.setExpectedResult(testCase.getExpectedResult());
        row.setTestData(testCase.getTestData());
        row.setSourceReferences(testCase.getSourceReferences());
        row.setRiskPoint(testCase.getRiskPoint());
        row.setAutomationSuggestion(testCase.getAutomationSuggestion());
        row.setQualityScore(testCase.getQualityScore());
        row.setDuplicateStatus(testCase.getDuplicateStatus());
        row.setDuplicateReason(testCase.getDuplicateReason());
        row.setSourceType(testCase.getSourceType());

        return row;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}