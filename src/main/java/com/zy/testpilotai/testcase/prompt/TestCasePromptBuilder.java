package com.zy.testpilotai.testcase.prompt;

import com.zy.testpilotai.knowledge.model.vo.RagContextVO;
import com.zy.testpilotai.testcase.model.dto.TestCaseGenerateRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
public class TestCasePromptBuilder {

    /**
     * 构建系统提示词。
     */
    public String buildSystemPrompt() {
        return """
                你是一名资深测试架构师，擅长根据 PRD、业务规则和接口约束设计高质量测试用例。
                
                你必须遵守以下规则：
                1. 只能基于用户提供的知识库上下文生成测试用例。
                2. 不要编造知识库中没有出现的业务规则。
                3. 测试用例必须可执行，不能只写抽象测试点。
                4. 每条用例必须包含明确步骤和明确预期结果。
                5. 每条用例必须包含来源引用 sourceReferences。
                6. 优先覆盖 P0 主流程、异常流程、边界场景、数据一致性和高风险场景。
                7. 必须严格输出 JSON，不要输出 Markdown，不要输出解释说明。
                """;
    }

    /**
     * 构建用户提示词。
     *
     * 用户提示词包含：
     * 1. 用户生成目标
     * 2. 选择的测试 Skill
     * 3. RAG 召回上下文
     * 4. 输出 JSON Schema
     */
    public String buildUserPrompt(TestCaseGenerateRequest request, RagContextVO ragContext) {
        StringBuilder builder = new StringBuilder();

        builder.append("【生成目标】\n")
                .append(request.getGenerateGoal())
                .append("\n\n");

        builder.append("【生成范围】\n")
                .append("项目ID：").append(request.getProjectId()).append("\n")
                .append("版本号：").append(request.getVersionNo() == null ? "不限制" : request.getVersionNo()).append("\n")
                .append("模块编码：").append(request.getModuleCode() == null ? "不限制" : request.getModuleCode()).append("\n")
                .append("生成类型：").append(request.getGenerateType()).append("\n\n");

        builder.append("【启用的测试 Skill】\n");
        if (CollectionUtils.isEmpty(request.getSelectedSkills())) {
            builder.append("默认启用：功能测试、异常测试、边界测试、数据一致性测试、风险场景测试\n\n");
        } else {
            for (String skill : request.getSelectedSkills()) {
                builder.append("- ").append(skill).append("\n");
            }
            builder.append("\n");
        }

        builder.append("【知识库上下文】\n")
                .append(ragContext.getContextText())
                .append("\n\n");

        builder.append("【测试设计要求】\n")
                .append("请至少从以下维度生成测试用例：\n")
                .append("1. 正常主流程\n")
                .append("2. 异常流程\n")
                .append("3. 边界条件\n")
                .append("4. 数据一致性\n")
                .append("5. 权限或状态限制\n")
                .append("6. 重复提交或并发风险\n")
                .append("7. 可自动化校验建议\n\n");

        builder.append("【输出 JSON 格式】\n")
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
                                  "sourceType": "RAG"
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