package com.zy.testpilotai.testcase.prompt;

import com.zy.testpilotai.knowledge.model.vo.RagContextVO;
import com.zy.testpilotai.testcase.model.entity.TestCase;
import com.zy.testpilotai.testcase.model.entity.TestCaseGenerateTask;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TestCaseReviewPromptBuilder {

    /**
     * 构建质量评审系统提示词。
     */
    public String buildReviewSystemPrompt() {
        return """
                你是一名资深测试架构师，负责评审 AI 生成的测试用例质量。
                
                你需要从测试专业角度评估：
                1. 是否覆盖主流程
                2. 是否覆盖异常流程
                3. 是否覆盖边界场景
                4. 是否覆盖安全和权限风险
                5. 是否覆盖数据一致性
                6. 是否具备可执行性
                7. 是否存在重复用例
                8. 是否有明确前置条件、步骤和预期结果
                9. 是否能追溯到需求来源
                
                你必须严格输出 JSON，不要输出 Markdown，不要输出解释说明。
                """;
    }

    /**
     * 构建质量评审用户提示词。
     */
    public String buildReviewUserPrompt(
            TestCaseGenerateTask task,
            List<TestCase> testCases
    ) {
        StringBuilder builder = new StringBuilder();

        builder.append("【被评审任务信息】\n")
                .append("任务ID：").append(task.getTaskId()).append("\n")
                .append("项目ID：").append(task.getProjectId()).append("\n")
                .append("版本号：").append(task.getVersionNo()).append("\n")
                .append("模块编码：").append(task.getModuleCode()).append("\n")
                .append("生成目标：").append(task.getGenerateGoal()).append("\n\n");

        builder.append("【待评审测试用例】\n");

        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);

            builder.append("用例 ").append(i + 1).append("：\n")
                    .append("标题：").append(testCase.getCaseTitle()).append("\n")
                    .append("类型：").append(testCase.getCaseType()).append("\n")
                    .append("优先级：").append(testCase.getPriority()).append("\n")
                    .append("前置条件：").append(testCase.getPrecondition()).append("\n")
                    .append("步骤：").append(testCase.getSteps()).append("\n")
                    .append("预期结果：").append(testCase.getExpectedResult()).append("\n")
                    .append("测试数据：").append(testCase.getTestData()).append("\n")
                    .append("来源引用：").append(testCase.getSourceReferences()).append("\n")
                    .append("风险点：").append(testCase.getRiskPoint()).append("\n")
                    .append("自动化建议：").append(testCase.getAutomationSuggestion()).append("\n\n");
        }

        builder.append("【评分规则】\n")
                .append("请按 100 分制评分：\n")
                .append("1. 主流程覆盖：20 分\n")
                .append("2. 异常流程覆盖：20 分\n")
                .append("3. 边界场景覆盖：15 分\n")
                .append("4. 数据一致性：15 分\n")
                .append("5. 可执行性：15 分\n")
                .append("6. 来源可追溯：15 分\n\n");

        builder.append("【质量评审输出 JSON 格式】\n")
                .append("""
                        {
                          "totalScore": 85,
                          "dimensions": [
                            {
                              "dimension": "主流程覆盖",
                              "score": 18,
                              "maxScore": 20,
                              "comment": "评价说明"
                            }
                          ],
                          "missingPoints": [
                            {
                              "type": "异常测试/边界测试/安全测试/数据一致性",
                              "priority": "P0/P1/P2/P3",
                              "description": "缺失测试点描述",
                              "suggestion": "补全建议"
                            }
                          ],
                          "duplicateCases": [
                            {
                              "caseTitle": "重复用例标题",
                              "reason": "重复原因"
                            }
                          ],
                          "lowQualityCases": [
                            {
                              "caseTitle": "低质量用例标题",
                              "reason": "质量问题"
                            }
                          ],
                          "summary": "整体评审总结"
                        }
                        """);

        return builder.toString();
    }

    /**
     * 构建缺失用例补全系统提示词。
     */
    public String buildCompleteSystemPrompt() {
        return """
                你是一名资深测试架构师，负责根据测试用例质量评审结果补全缺失测试用例。
                
                你必须遵守以下规则：
                1. 只能围绕缺失测试点补充用例。
                2. 不要重复已有测试用例。
                3. 补全用例必须可执行。
                4. 每条用例必须包含明确步骤和明确预期结果。
                5. 每条用例必须包含 sourceReferences。
                6. 必须严格输出 JSON，不要输出 Markdown，不要输出解释说明。
                """;
    }

    /**
     * 构建缺失用例补全用户提示词。
     */
    public String buildCompleteUserPrompt(
            TestCaseGenerateTask task,
            List<TestCase> existingCases,
            String missingPoints,
            RagContextVO ragContext
    ) {
        StringBuilder builder = new StringBuilder();

        builder.append("【原始生成任务】\n")
                .append("任务ID：").append(task.getTaskId()).append("\n")
                .append("项目ID：").append(task.getProjectId()).append("\n")
                .append("版本号：").append(task.getVersionNo()).append("\n")
                .append("模块编码：").append(task.getModuleCode()).append("\n")
                .append("生成目标：").append(task.getGenerateGoal()).append("\n\n");

        builder.append("【已有测试用例，请不要重复生成】\n");
        for (int i = 0; i < existingCases.size(); i++) {
            TestCase testCase = existingCases.get(i);
            builder.append(i + 1)
                    .append(". ")
                    .append(testCase.getCaseTitle())
                    .append("，类型：")
                    .append(testCase.getCaseType())
                    .append("，预期：")
                    .append(testCase.getExpectedResult())
                    .append("\n");
        }

        builder.append("\n【质量评审发现的缺失测试点】\n")
                .append(missingPoints)
                .append("\n\n");

        builder.append("【知识库上下文】\n")
                .append(ragContext.getContextText())
                .append("\n\n");

        builder.append("【补全要求】\n")
                .append("请只根据缺失测试点补充新的测试用例，不要重复已有用例。\n")
                .append("优先补充 P0 / P1 风险场景。\n\n");

        builder.append("【缺失用例补全输出 JSON 格式】\n")
                .append("""
                        {
                          "testCases": [
                            {
                              "moduleName": "模块名称",
                              "caseTitle": "用例标题",
                              "caseType": "功能测试/异常测试/边界测试/安全测试/回归测试",
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
                                  "sourceType": "REVIEW_MISSING_POINT"
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