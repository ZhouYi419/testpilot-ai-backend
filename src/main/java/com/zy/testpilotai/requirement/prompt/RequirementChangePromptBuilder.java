package com.zy.testpilotai.requirement.prompt;

import com.zy.testpilotai.knowledge.model.vo.RagContextVO;
import com.zy.testpilotai.requirement.model.dto.ChangeImpactAnalyzeRequest;
import com.zy.testpilotai.requirement.model.dto.IncrementalTestCaseGenerateRequest;
import com.zy.testpilotai.requirement.model.entity.RequirementChangeAnalysisTask;
import com.zy.testpilotai.testcase.model.entity.TestCase;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import java.util.List;

@Component
public class RequirementChangePromptBuilder {

    /**
     * 构建影响分析系统提示词。
     */
    public String buildImpactSystemPrompt() {
        return """
                你是一名资深测试架构师，擅长根据新需求、旧版本 PRD 知识库和历史测试用例分析测试影响范围。
                
                你必须完成以下任务：
                1. 判断新需求影响哪些功能模块。
                2. 分析新需求和旧版本规则的关系。
                3. 找出需要新增测试的场景。
                4. 找出需要回归测试的旧功能。
                5. 找出高风险点。
                6. 不要编造知识库中不存在的旧规则。
                7. 必须严格输出 JSON，不要输出 Markdown，不要输出解释说明。
                """;
    }

    /**
     * 构建影响分析用户提示词。
     */
    public String buildImpactUserPrompt(
            ChangeImpactAnalyzeRequest request,
            RagContextVO oldVersionContext,
            List<TestCase> historicalCases
    ) {
        StringBuilder builder = new StringBuilder();

        builder.append("【项目与版本信息】\n")
                .append("项目ID：").append(request.getProjectId()).append("\n")
                .append("基线版本：").append(request.getBaseVersionNo()).append("\n")
                .append("目标版本：").append(request.getTargetVersionNo()).append("\n\n");

        builder.append("【新需求】\n")
                .append(request.getNewRequirement())
                .append("\n\n");

        builder.append("【旧版本知识库上下文】\n")
                .append(oldVersionContext.getContextText())
                .append("\n\n");

        builder.append("【历史测试用例】\n");
        if (historicalCases == null || historicalCases.isEmpty()) {
            builder.append("暂无历史测试用例。\n\n");
        } else {
            for (int i = 0; i < historicalCases.size(); i++) {
                TestCase testCase = historicalCases.get(i);
                builder.append("历史用例 ").append(i + 1).append("：\n")
                        .append("标题：").append(testCase.getCaseTitle()).append("\n")
                        .append("模块：").append(testCase.getModuleName()).append("(").append(testCase.getModuleCode()).append(")\n")
                        .append("类型：").append(testCase.getCaseType()).append("\n")
                        .append("优先级：").append(testCase.getPriority()).append("\n")
                        .append("预期结果：").append(testCase.getExpectedResult()).append("\n\n");
            }
        }

        builder.append("【分析要求】\n")
                .append("请从以下维度分析：\n")
                .append("1. 新需求变更摘要。\n")
                .append("2. 影响模块及影响等级。\n")
                .append("3. 旧版本相关业务规则。\n")
                .append("4. 高风险点。\n")
                .append("5. 回归测试范围。\n")
                .append("6. 建议新增测试点。\n\n");

        builder.append("【影响分析输出 JSON 格式】\n")
                .append("""
                        {
                          "changeSummary": [
                            "变更点1",
                            "变更点2"
                          ],
                          "affectedModules": [
                            {
                              "moduleCode": "模块编码",
                              "moduleName": "模块名称",
                              "impactLevel": "HIGH/MEDIUM/LOW",
                              "reason": "影响原因"
                            }
                          ],
                          "relatedOldRules": [
                            {
                              "versionNo": "旧版本号",
                              "moduleCode": "模块编码",
                              "rule": "旧版本相关规则"
                            }
                          ],
                          "riskPoints": [
                            {
                              "riskLevel": "HIGH/MEDIUM/LOW",
                              "description": "风险描述",
                              "suggestion": "测试建议"
                            }
                          ],
                          "regressionScope": [
                            {
                              "moduleCode": "模块编码",
                              "moduleName": "模块名称",
                              "reason": "为什么需要回归"
                            }
                          ],
                          "suggestedNewTestPoints": [
                            {
                              "type": "功能测试/异常测试/边界测试/数据一致性/回归测试",
                              "priority": "P0/P1/P2/P3",
                              "description": "建议测试点描述"
                            }
                          ],
                          "summary": "整体分析总结"
                        }
                        """);

        return builder.toString();
    }

    /**
     * 构建增量测试用例生成系统提示词。
     */
    public String buildIncrementalSystemPrompt() {
        return """
                你是一名资深测试架构师，负责根据新需求影响分析结果生成新版本增量测试用例。
                
                你必须遵守以下规则：
                1. 测试用例必须基于新需求、旧版本知识库和影响分析结果。
                2. 必须区分新增测试、回归测试、异常测试、边界测试、数据一致性测试。
                3. 不要重复历史已有用例，回归用例可以引用历史用例思想，但要说明回归目的。
                4. 每条用例必须可执行。
                5. 每条用例必须有明确步骤和明确预期结果。
                6. 每条用例必须有 sourceReferences。
                7. 必须严格输出 JSON，不要输出 Markdown，不要输出解释说明。
                """;
    }

    /**
     * 构建增量测试用例生成用户提示词。
     */
    public String buildIncrementalUserPrompt(
            IncrementalTestCaseGenerateRequest request,
            RequirementChangeAnalysisTask analysisTask,
            RagContextVO oldVersionContext,
            List<TestCase> historicalCases
    ) {
        StringBuilder builder = new StringBuilder();

        builder.append("【项目与版本信息】\n")
                .append("项目ID：").append(analysisTask.getProjectId()).append("\n")
                .append("基线版本：").append(analysisTask.getBaseVersionNo()).append("\n")
                .append("目标版本：").append(analysisTask.getTargetVersionNo()).append("\n\n");

        builder.append("【新需求】\n")
                .append(analysisTask.getNewRequirement())
                .append("\n\n");

        builder.append("【影响分析结果】\n")
                .append("变更摘要：").append(analysisTask.getChangeSummary()).append("\n")
                .append("影响模块：").append(analysisTask.getAffectedModules()).append("\n")
                .append("相关旧规则：").append(analysisTask.getRelatedOldRules()).append("\n")
                .append("风险点：").append(analysisTask.getRiskPoints()).append("\n")
                .append("回归范围：").append(analysisTask.getRegressionScope()).append("\n")
                .append("建议新增测试点：").append(analysisTask.getSuggestedNewTestPoints()).append("\n\n");

        builder.append("【旧版本知识库上下文】\n")
                .append(oldVersionContext.getContextText())
                .append("\n\n");

        builder.append("【历史测试用例，请避免重复】\n");
        if (historicalCases == null || historicalCases.isEmpty()) {
            builder.append("暂无历史测试用例。\n\n");
        } else {
            for (int i = 0; i < historicalCases.size(); i++) {
                TestCase testCase = historicalCases.get(i);
                builder.append(i + 1)
                        .append(". ")
                        .append(testCase.getCaseTitle())
                        .append("，类型：")
                        .append(testCase.getCaseType())
                        .append("，预期：")
                        .append(testCase.getExpectedResult())
                        .append("\n");
            }
            builder.append("\n");
        }

        builder.append("【启用的测试 Skill】\n");
        if (CollectionUtils.isEmpty(request.getSelectedSkills())) {
            builder.append("默认启用：增量功能测试、异常测试、边界测试、数据一致性测试、回归测试。\n\n");
        } else {
            for (String skill : request.getSelectedSkills()) {
                builder.append("- ").append(skill).append("\n");
            }
            builder.append("\n");
        }

        builder.append("【生成要求】\n")
                .append("请生成目标版本的新测试用例，至少包含：\n")
                .append("1. 新需求主流程用例。\n")
                .append("2. 新需求异常流程用例。\n")
                .append("3. 新需求边界场景用例。\n")
                .append("4. 数据一致性用例。\n")
                .append("5. 旧功能回归用例。\n\n");

        builder.append("【增量测试用例输出 JSON 格式】\n")
                .append("""
                        {
                          "testCases": [
                            {
                              "moduleName": "模块名称",
                              "caseTitle": "用例标题",
                              "caseType": "增量功能测试/异常测试/边界测试/数据一致性/回归测试",
                              "priority": "P0/P1/P2/P3",
                              "precondition": "前置条件",
                              "steps": [
                                "步骤1",
                                "步骤2"
                              ],
                              "expectedResult": "预期结果",
                              "testData": {
                                "key": "value"
                              },
                              "sourceReferences": [
                                {
                                  "versionNo": "版本号",
                                  "sectionTitle": "章节标题",
                                  "sourceType": "NEW_REQUIREMENT/OLD_VERSION_REGRESSION/IMPACT_ANALYSIS"
                                }
                              ],
                              "riskPoint": "风险点",
                              "automationSuggestion": "自动化建议"
                            }
                          ]
                        }
                        """);

        return builder.toString();
    }
}