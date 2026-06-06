package com.zy.testpilotai.report.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zy.testpilotai.agent.mapper.AgentTaskMapper;
import com.zy.testpilotai.agent.mapper.AgentTaskStepMapper;
import com.zy.testpilotai.agent.model.entity.AgentTask;
import com.zy.testpilotai.agent.model.entity.AgentTaskStep;
import com.zy.testpilotai.agent.model.vo.AgentExecutionLogVO;
import com.zy.testpilotai.agent.service.AgentExecutionLogService;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.report.model.dto.AgentReportExcelExportRequest;
import com.zy.testpilotai.report.model.dto.TestCaseCompareExcelExportRequest;
import com.zy.testpilotai.report.model.dto.TestCaseSetExcelExportRequest;
import com.zy.testpilotai.report.service.TestReportExportService;
import com.zy.testpilotai.testcase.mapper.TestCaseMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSetCompareResultMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSetCompareTaskMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSetItemMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSetMapper;
import com.zy.testpilotai.testcase.model.entity.TestCase;
import com.zy.testpilotai.testcase.model.entity.TestCaseSet;
import com.zy.testpilotai.testcase.model.entity.TestCaseSetCompareResult;
import com.zy.testpilotai.testcase.model.entity.TestCaseSetCompareTask;
import com.zy.testpilotai.testcase.model.entity.TestCaseSetItem;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TestReportExportServiceImpl implements TestReportExportService {

    private final TestCaseSetMapper testCaseSetMapper;

    private final TestCaseSetItemMapper testCaseSetItemMapper;

    private final TestCaseMapper testCaseMapper;

    private final TestCaseSetCompareTaskMapper compareTaskMapper;

    private final TestCaseSetCompareResultMapper compareResultMapper;

    private final AgentTaskMapper agentTaskMapper;

    private final AgentTaskStepMapper agentTaskStepMapper;

    private final AgentExecutionLogService agentExecutionLogService;

    @Override
    public void exportCaseSet(
            TestCaseSetExcelExportRequest request,
            HttpServletResponse response
    ) {
        if (request == null || !StringUtils.hasText(request.getCaseSetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "用例集 ID 不能为空"
            );
        }

        TestCaseSet caseSet = getActiveCaseSet(request.getCaseSetId());

        List<TestCaseSetItem> items = testCaseSetItemMapper.selectList(
                new LambdaQueryWrapper<TestCaseSetItem>()
                        .eq(TestCaseSetItem::getCaseSetId, caseSet.getCaseSetId())
                        .orderByAsc(TestCaseSetItem::getItemOrder)
                        .orderByAsc(TestCaseSetItem::getId)
        );

        List<SheetData> sheets = new ArrayList<>();

        sheets.add(new SheetData(
                "用例集信息",
                head("字段", "值"),
                buildCaseSetInfoRows(caseSet)
        ));

        sheets.add(new SheetData(
                "测试用例",
                head(
                        "序号",
                        "用例ID",
                        "任务ID",
                        "项目ID",
                        "版本号",
                        "模块编码",
                        "模块名称",
                        "用例标题",
                        "用例类型",
                        "优先级",
                        "前置条件",
                        "测试步骤",
                        "预期结果",
                        "测试数据",
                        "来源引用",
                        "风险点",
                        "自动化建议",
                        "质量评分",
                        "重复状态",
                        "来源类型",
                        "审核状态",
                        "审核人",
                        "人工备注",
                        "创建时间"
                ),
                buildCaseSetCaseRows(items)
        ));

        writeExcel(
                response,
                safeFileName(caseSet.getSetName()) + "_用例集.xlsx",
                sheets
        );
    }

    @Override
    public void exportCompare(
            TestCaseCompareExcelExportRequest request,
            HttpServletResponse response
    ) {
        if (request == null || !StringUtils.hasText(request.getCompareTaskId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "对比任务 ID 不能为空"
            );
        }

        TestCaseSetCompareTask task = getCompareTask(request.getCompareTaskId());

        List<TestCaseSetCompareResult> results = compareResultMapper.selectList(
                new LambdaQueryWrapper<TestCaseSetCompareResult>()
                        .eq(TestCaseSetCompareResult::getCompareTaskId, task.getCompareTaskId())
                        .orderByAsc(TestCaseSetCompareResult::getId)
        );

        List<SheetData> sheets = new ArrayList<>();

        sheets.add(new SheetData(
                "对比概览",
                head("字段", "值"),
                buildCompareOverviewRows(task)
        ));

        sheets.add(new SheetData(
                "新增用例",
                compareResultHead(),
                buildCompareResultRows(results, "ADDED")
        ));

        sheets.add(new SheetData(
                "删除用例",
                compareResultHead(),
                buildCompareResultRows(results, "REMOVED")
        ));

        sheets.add(new SheetData(
                "修改用例",
                compareResultHead(),
                buildCompareResultRows(results, "MODIFIED")
        ));

        sheets.add(new SheetData(
                "未变化用例",
                compareResultHead(),
                buildCompareResultRows(results, "UNCHANGED")
        ));

        writeExcel(
                response,
                "用例集版本对比_" + task.getCompareTaskId() + ".xlsx",
                sheets
        );
    }

    @Override
    public void exportAgentReport(
            AgentReportExcelExportRequest request,
            HttpServletResponse response
    ) {
        if (request == null || !StringUtils.hasText(request.getAgentTaskId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "Agent 任务 ID 不能为空"
            );
        }

        AgentTask agentTask = getAgentTask(request.getAgentTaskId());

        List<AgentTaskStep> steps = agentTaskStepMapper.selectList(
                new LambdaQueryWrapper<AgentTaskStep>()
                        .eq(AgentTaskStep::getAgentTaskId, agentTask.getAgentTaskId())
                        .orderByAsc(AgentTaskStep::getStepIndex)
                        .orderByAsc(AgentTaskStep::getId)
        );

        List<AgentExecutionLogVO> logs = agentExecutionLogService.listByAgentTaskId(
                agentTask.getAgentTaskId()
        );

        List<SheetData> sheets = new ArrayList<>();

        sheets.add(new SheetData(
                "Agent任务信息",
                head("字段", "值"),
                buildAgentInfoRows(agentTask)
        ));

        sheets.add(new SheetData(
                "执行步骤",
                head(
                        "序号",
                        "步骤序号",
                        "步骤类型",
                        "步骤名称",
                        "状态",
                        "输入",
                        "输出",
                        "错误信息",
                        "是否可重试",
                        "重试次数",
                        "开始时间",
                        "结束时间"
                ),
                buildAgentStepRows(steps)
        ));

        sheets.add(new SheetData(
                "执行日志",
                head(
                        "序号",
                        "日志ID",
                        "Agent任务ID",
                        "步骤序号",
                        "步骤类型",
                        "步骤名称",
                        "日志级别",
                        "事件类型",
                        "消息",
                        "输入",
                        "输出",
                        "耗时ms",
                        "创建时间"
                ),
                buildAgentLogRows(logs)
        ));

        writeExcel(
                response,
                "Agent执行报告_" + agentTask.getAgentTaskId() + ".xlsx",
                sheets
        );
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
                    "用例集不存在或已删除"
            );
        }

        return caseSet;
    }

    private TestCaseSetCompareTask getCompareTask(String compareTaskId) {
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

        return task;
    }

    private AgentTask getAgentTask(String agentTaskId) {
        AgentTask task = agentTaskMapper.selectOne(
                new LambdaQueryWrapper<AgentTask>()
                        .eq(AgentTask::getAgentTaskId, agentTaskId)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "Agent 任务不存在"
            );
        }

        return task;
    }

    private List<List<Object>> buildCaseSetInfoRows(TestCaseSet caseSet) {
        List<List<Object>> rows = new ArrayList<>();

        rows.add(row("用例集ID", caseSet.getCaseSetId()));
        rows.add(row("项目ID", caseSet.getProjectId()));
        rows.add(row("版本号", caseSet.getVersionNo()));
        rows.add(row("模块编码", caseSet.getModuleCode()));
        rows.add(row("用例集名称", caseSet.getSetName()));
        rows.add(row("用例集类型", caseSet.getSetType()));
        rows.add(row("描述", caseSet.getDescription()));
        rows.add(row("用例数量", caseSet.getCaseCount()));
        rows.add(row("状态", caseSet.getStatus()));
        rows.add(row("创建时间", caseSet.getCreateTime()));
        rows.add(row("更新时间", caseSet.getUpdateTime()));

        return rows;
    }

    private List<List<Object>> buildCaseSetCaseRows(List<TestCaseSetItem> items) {
        List<List<Object>> rows = new ArrayList<>();

        int index = 1;

        for (TestCaseSetItem item : items) {
            TestCase testCase = testCaseMapper.selectById(item.getTestCaseId());

            if (testCase == null) {
                continue;
            }

            rows.add(row(
                    index++,
                    testCase.getId(),
                    testCase.getTaskId(),
                    testCase.getProjectId(),
                    testCase.getVersionNo(),
                    testCase.getModuleCode(),
                    testCase.getModuleName(),
                    testCase.getCaseTitle(),
                    testCase.getCaseType(),
                    testCase.getPriority(),
                    testCase.getPrecondition(),
                    testCase.getSteps(),
                    testCase.getExpectedResult(),
                    testCase.getTestData(),
                    testCase.getSourceReferences(),
                    testCase.getRiskPoint(),
                    testCase.getAutomationSuggestion(),
                    testCase.getQualityScore(),
                    testCase.getDuplicateStatus(),
                    testCase.getSourceType(),
                    testCase.getReviewStatus(),
                    testCase.getReviewer(),
                    testCase.getManualComment(),
                    testCase.getCreateTime()
            ));
        }

        return rows;
    }

    private List<List<Object>> buildCompareOverviewRows(TestCaseSetCompareTask task) {
        List<List<Object>> rows = new ArrayList<>();

        rows.add(row("对比任务ID", task.getCompareTaskId()));
        rows.add(row("项目ID", task.getProjectId()));
        rows.add(row("源用例集ID", task.getSourceCaseSetId()));
        rows.add(row("目标用例集ID", task.getTargetCaseSetId()));
        rows.add(row("状态", task.getStatus()));
        rows.add(row("新增数量", task.getAddedCount()));
        rows.add(row("删除数量", task.getRemovedCount()));
        rows.add(row("修改数量", task.getModifiedCount()));
        rows.add(row("未变化数量", task.getUnchangedCount()));
        rows.add(row("汇总JSON", task.getSummary()));
        rows.add(row("错误信息", task.getErrorMessage()));
        rows.add(row("创建时间", task.getCreateTime()));
        rows.add(row("更新时间", task.getUpdateTime()));

        return rows;
    }

    private List<List<Object>> buildCompareResultRows(
            List<TestCaseSetCompareResult> results,
            String resultType
    ) {
        List<List<Object>> rows = new ArrayList<>();

        int index = 1;

        for (TestCaseSetCompareResult result : results) {
            if (!resultType.equals(result.getResultType())) {
                continue;
            }

            rows.add(row(
                    index++,
                    result.getId(),
                    result.getCompareTaskId(),
                    result.getResultType(),
                    result.getSourceTestCaseId(),
                    result.getTargetTestCaseId(),
                    result.getSourceCaseTitle(),
                    result.getTargetCaseTitle(),
                    result.getChangeSummary(),
                    result.getFieldDiffs(),
                    result.getCreateTime()
            ));
        }

        return rows;
    }

    private List<List<Object>> buildAgentInfoRows(AgentTask task) {
        List<List<Object>> rows = new ArrayList<>();

        rows.add(row("Agent任务ID", task.getAgentTaskId()));
        rows.add(row("工作流类型", task.getWorkflowType()));
        rows.add(row("项目ID", task.getProjectId()));
        rows.add(row("基线版本", task.getBaseVersionNo()));
        rows.add(row("目标版本", task.getTargetVersionNo()));
        rows.add(row("模块编码", task.getModuleCode()));
        rows.add(row("用户目标", task.getUserGoal()));
        rows.add(row("新需求", task.getNewRequirement()));
        rows.add(row("AI应用类型", task.getAppType()));
        rows.add(row("AI应用说明", task.getAppDescription()));
        rows.add(row("选择的Skill", task.getSelectedSkills()));
        rows.add(row("状态", task.getStatus()));
        rows.add(row("影响分析任务ID", task.getAnalysisTaskId()));
        rows.add(row("测试用例任务ID", task.getTestcaseTaskId()));
        rows.add(row("AI应用测试任务ID", task.getAiAppTaskId()));
        rows.add(row("最终结果", task.getFinalResult()));
        rows.add(row("错误信息", task.getErrorMessage()));
        rows.add(row("执行模式", task.getExecutionMode()));
        rows.add(row("当前步骤", task.getCurrentStepIndex()));
        rows.add(row("重试次数", task.getRetryCount()));
        rows.add(row("从第几步恢复", task.getResumeFromStep()));
        rows.add(row("topK", task.getTopK()));
        rows.add(row("自动评审", task.getAutoReview()));
        rows.add(row("自动补全", task.getAutoCompleteMissing()));
        rows.add(row("自动去重", task.getAutoDeduplicate()));
        rows.add(row("去重阈值", task.getDeduplicateThreshold()));
        rows.add(row("AI应用测试维度", task.getTestDimensions()));
        rows.add(row("开始时间", task.getStartTime()));
        rows.add(row("结束时间", task.getEndTime()));
        rows.add(row("创建时间", task.getCreateTime()));
        rows.add(row("更新时间", task.getUpdateTime()));

        return rows;
    }

    private List<List<Object>> buildAgentStepRows(List<AgentTaskStep> steps) {
        List<List<Object>> rows = new ArrayList<>();

        int index = 1;

        for (AgentTaskStep step : steps) {
            rows.add(row(
                    index++,
                    step.getStepIndex(),
                    step.getStepType(),
                    step.getStepName(),
                    step.getStatus(),
                    step.getInput(),
                    step.getOutput(),
                    step.getErrorMessage(),
                    step.getRetryable(),
                    step.getRetryCount(),
                    step.getStartTime(),
                    step.getEndTime()
            ));
        }

        return rows;
    }

    private List<List<Object>> buildAgentLogRows(List<AgentExecutionLogVO> logs) {
        List<List<Object>> rows = new ArrayList<>();

        int index = 1;

        for (AgentExecutionLogVO log : logs) {
            rows.add(row(
                    index++,
                    beanValue(log, "id"),
                    beanValue(log, "agentTaskId"),
                    beanValue(log, "stepIndex"),
                    beanValue(log, "stepType"),
                    beanValue(log, "stepName"),
                    beanValue(log, "logLevel"),
                    beanValue(log, "eventType"),
                    beanValue(log, "message"),
                    beanValue(log, "input"),
                    beanValue(log, "output"),
                    beanValue(log, "durationMs"),
                    beanValue(log, "createTime")
            ));
        }

        return rows;
    }

    private Object beanValue(Object bean, String propertyName) {
        if (bean == null || !StringUtils.hasText(propertyName)) {
            return null;
        }

        try {
            BeanWrapper wrapper = new BeanWrapperImpl(bean);

            if (!wrapper.isReadableProperty(propertyName)) {
                return null;
            }

            return wrapper.getPropertyValue(propertyName);
        } catch (Exception e) {
            return null;
        }
    }

    private List<List<String>> compareResultHead() {
        return head(
                "序号",
                "结果ID",
                "对比任务ID",
                "结果类型",
                "源用例ID",
                "目标用例ID",
                "源用例标题",
                "目标用例标题",
                "变更说明",
                "字段差异JSON",
                "创建时间"
        );
    }

    private List<List<String>> head(String... names) {
        List<List<String>> head = new ArrayList<>();

        for (String name : names) {
            head.add(List.of(name));
        }

        return head;
    }

    private List<Object> row(Object... values) {
        return new ArrayList<>(List.of(values));
    }

    private void writeExcel(
            HttpServletResponse response,
            String fileName,
            List<SheetData> sheets
    ) {
        try {
            response.setContentType(
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            );
            response.setCharacterEncoding("UTF-8");

            String encodedFileName = URLEncoder.encode(
                    fileName,
                    StandardCharsets.UTF_8
            ).replaceAll("\\+", "%20");

            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename*=UTF-8''" + encodedFileName
            );

            try (ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream()).build()) {
                int sheetNo = 0;

                for (SheetData sheetData : sheets) {
                    WriteSheet writeSheet = EasyExcel.writerSheet(
                            sheetNo++,
                            sheetData.sheetName()
                    ).head(sheetData.head()).build();

                    excelWriter.write(sheetData.rows(), writeSheet);
                }
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "Excel 导出失败：" + e.getMessage()
            );
        }
    }

    private String safeFileName(String value) {
        if (!StringUtils.hasText(value)) {
            return "测试报告";
        }

        return value
                .replaceAll("[\\\\/:*?\"<>|]", "_")
                .replaceAll("\\s+", "_");
    }

    private record SheetData(
            String sheetName,
            List<List<String>> head,
            List<List<Object>> rows
    ) {
    }
}