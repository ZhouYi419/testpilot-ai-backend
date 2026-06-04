package com.zy.testpilotai.skill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.skill.mapper.SkillDefinitionMapper;
import com.zy.testpilotai.skill.model.dto.SkillCreateRequest;
import com.zy.testpilotai.skill.model.dto.SkillUpdateRequest;
import com.zy.testpilotai.skill.model.entity.SkillDefinition;
import com.zy.testpilotai.skill.model.vo.SkillDefinitionVO;
import com.zy.testpilotai.skill.service.SkillDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SkillDefinitionServiceImpl implements SkillDefinitionService {

    private final SkillDefinitionMapper skillDefinitionMapper;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<SkillDefinitionVO> initBuiltinSkills() {
        /*
         * 初始化内置 Skill。
         */
        List<SkillDefinition> builtinSkills = buildBuiltinSkills();

        for (SkillDefinition skill : builtinSkills) {
            SkillDefinition existing = skillDefinitionMapper.selectOne(
                    new LambdaQueryWrapper<SkillDefinition>()
                            .eq(SkillDefinition::getSkillCode, skill.getSkillCode())
                            .last("LIMIT 1")
            );

            if (existing == null) {
                skill.setCreateTime(LocalDateTime.now());
                skill.setUpdateTime(LocalDateTime.now());
                skillDefinitionMapper.insert(skill);
            } else {
                existing.setSkillName(skill.getSkillName());
                existing.setSkillType(skill.getSkillType());
                existing.setDescription(skill.getDescription());
                existing.setPromptTemplate(skill.getPromptTemplate());
                existing.setOutputSchema(skill.getOutputSchema());
                existing.setRubric(skill.getRubric());
                existing.setVersionNo(skill.getVersionNo());
                existing.setEnabled(skill.getEnabled());
                existing.setUpdateTime(LocalDateTime.now());
                skillDefinitionMapper.updateById(existing);
            }
        }

        return list(false);
    }

    @Override
    public SkillDefinitionVO create(SkillCreateRequest request) {
        SkillDefinition existing = skillDefinitionMapper.selectOne(
                new LambdaQueryWrapper<SkillDefinition>()
                        .eq(SkillDefinition::getSkillCode, request.getSkillCode())
                        .last("LIMIT 1")
        );

        if (existing != null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "Skill 编码已存在");
        }

        SkillDefinition skill = new SkillDefinition();
        skill.setSkillCode(request.getSkillCode());
        skill.setSkillName(request.getSkillName());
        skill.setSkillType(request.getSkillType());
        skill.setDescription(request.getDescription());
        skill.setPromptTemplate(request.getPromptTemplate());
        skill.setOutputSchema(normalizeJson(request.getOutputSchema(), "{}"));
        skill.setRubric(normalizeJson(request.getRubric(), "[]"));
        skill.setVersionNo(StringUtils.hasText(request.getVersionNo()) ? request.getVersionNo() : "1.0.0");
        skill.setEnabled(1);
        skill.setCreateTime(LocalDateTime.now());
        skill.setUpdateTime(LocalDateTime.now());

        skillDefinitionMapper.insert(skill);

        return toVO(skill);
    }

    @Override
    public SkillDefinitionVO update(SkillUpdateRequest request) {
        SkillDefinition skill = skillDefinitionMapper.selectById(request.getId());

        if (skill == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Skill 不存在");
        }

        if (StringUtils.hasText(request.getSkillName())) {
            skill.setSkillName(request.getSkillName());
        }

        if (StringUtils.hasText(request.getSkillType())) {
            skill.setSkillType(request.getSkillType());
        }

        if (request.getDescription() != null) {
            skill.setDescription(request.getDescription());
        }

        if (StringUtils.hasText(request.getPromptTemplate())) {
            skill.setPromptTemplate(request.getPromptTemplate());
        }

        if (request.getOutputSchema() != null) {
            skill.setOutputSchema(normalizeJson(request.getOutputSchema(), "{}"));
        }

        if (request.getRubric() != null) {
            skill.setRubric(normalizeJson(request.getRubric(), "[]"));
        }

        if (StringUtils.hasText(request.getVersionNo())) {
            skill.setVersionNo(request.getVersionNo());
        }

        skill.setUpdateTime(LocalDateTime.now());
        skillDefinitionMapper.updateById(skill);

        return toVO(skill);
    }

    @Override
    public List<SkillDefinitionVO> list(Boolean enabledOnly) {
        LambdaQueryWrapper<SkillDefinition> wrapper =
                new LambdaQueryWrapper<SkillDefinition>()
                        .orderByAsc(SkillDefinition::getId);

        if (Boolean.TRUE.equals(enabledOnly)) {
            wrapper.eq(SkillDefinition::getEnabled, 1);
        }

        return skillDefinitionMapper.selectList(wrapper)
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public SkillDefinitionVO getByCode(String skillCode) {
        SkillDefinition skill = getEnabledOrAnyByCode(skillCode, false);
        return toVO(skill);
    }

    @Override
    public Boolean enable(String skillCode) {
        SkillDefinition skill = getEnabledOrAnyByCode(skillCode, false);
        skill.setEnabled(1);
        skill.setUpdateTime(LocalDateTime.now());
        skillDefinitionMapper.updateById(skill);
        return true;
    }

    @Override
    public Boolean disable(String skillCode) {
        SkillDefinition skill = getEnabledOrAnyByCode(skillCode, false);
        skill.setEnabled(0);
        skill.setUpdateTime(LocalDateTime.now());
        skillDefinitionMapper.updateById(skill);
        return true;
    }

    @Override
    public String buildGenerationSkillPrompt(List<String> selectedSkills) {
        List<SkillDefinition> skills = querySelectedEnabledSkills(selectedSkills);

        if (skills.isEmpty()) {
            return buildDefaultGenerationSkillPrompt();
        }

        StringBuilder builder = new StringBuilder();
        builder.append("以下是本次测试设计启用的 Skill，请严格按照这些测试方法设计用例：\n\n");

        for (SkillDefinition skill : skills) {
            builder.append("【")
                    .append(skill.getSkillCode())
                    .append(" - ")
                    .append(skill.getSkillName())
                    .append("】\n")
                    .append(skill.getPromptTemplate())
                    .append("\n\n");
        }

        return builder.toString();
    }

    @Override
    public String buildReviewRubricPrompt(List<String> selectedSkills) {
        List<SkillDefinition> skills = querySelectedEnabledSkills(selectedSkills);

        if (skills.isEmpty()) {
            return buildDefaultReviewRubricPrompt();
        }

        StringBuilder builder = new StringBuilder();
        builder.append("以下是本次质量评审需要参考的 Skill Rubric：\n\n");

        for (SkillDefinition skill : skills) {
            builder.append("【")
                    .append(skill.getSkillCode())
                    .append(" - ")
                    .append(skill.getSkillName())
                    .append(" 评分规则】\n")
                    .append(skill.getRubric())
                    .append("\n\n");
        }

        return builder.toString();
    }

    @Override
    public String buildGenerationSkillPromptFromJson(String selectedSkillsJson) {
        return buildGenerationSkillPrompt(parseSkillCodes(selectedSkillsJson));
    }

    @Override
    public String buildReviewRubricPromptFromJson(String selectedSkillsJson) {
        return buildReviewRubricPrompt(parseSkillCodes(selectedSkillsJson));
    }

    private List<SkillDefinition> querySelectedEnabledSkills(List<String> selectedSkills) {
        /*
         * 如果用户没有选择 Skill，则默认使用全部启用的测试设计类 Skill。
         */
        if (CollectionUtils.isEmpty(selectedSkills)) {
            return skillDefinitionMapper.selectList(
                    new LambdaQueryWrapper<SkillDefinition>()
                            .eq(SkillDefinition::getEnabled, 1)
                            .in(SkillDefinition::getSkillType, List.of("TEST_DESIGN", "REGRESSION", "AI_APP_TEST"))
                            .orderByAsc(SkillDefinition::getId)
            );
        }

        return skillDefinitionMapper.selectList(
                new LambdaQueryWrapper<SkillDefinition>()
                        .eq(SkillDefinition::getEnabled, 1)
                        .in(SkillDefinition::getSkillCode, selectedSkills)
                        .orderByAsc(SkillDefinition::getId)
        );
    }

    private List<String> parseSkillCodes(String selectedSkillsJson) {
        if (!StringUtils.hasText(selectedSkillsJson) || "null".equals(selectedSkillsJson)) {
            return List.of();
        }

        try {
            return objectMapper.readValue(selectedSkillsJson, new TypeReference<List<String>>() {
            });
        } catch (Exception e) {
            /*
             * 如果历史数据格式异常，不影响主流程，降级为默认 Skill。
             */
            return List.of();
        }
    }

    private SkillDefinition getEnabledOrAnyByCode(String skillCode, boolean enabledOnly) {
        LambdaQueryWrapper<SkillDefinition> wrapper =
                new LambdaQueryWrapper<SkillDefinition>()
                        .eq(SkillDefinition::getSkillCode, skillCode)
                        .last("LIMIT 1");

        if (enabledOnly) {
            wrapper.eq(SkillDefinition::getEnabled, 1);
        }

        SkillDefinition skill = skillDefinitionMapper.selectOne(wrapper);

        if (skill == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "Skill 不存在：" + skillCode);
        }

        return skill;
    }

    private String normalizeJson(String json, String defaultValue) {
        if (!StringUtils.hasText(json)) {
            return defaultValue;
        }

        try {
            objectMapper.readTree(json);
            return json;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "JSON 格式不正确：" + e.getMessage());
        }
    }

    private String buildDefaultGenerationSkillPrompt() {
        return """
                默认测试设计 Skill：
                1. 覆盖正常主流程。
                2. 覆盖异常流程。
                3. 覆盖边界场景。
                4. 覆盖数据一致性。
                5. 覆盖高风险和回归场景。
                """;
    }

    private String buildDefaultReviewRubricPrompt() {
        return """
                默认质量评审规则：
                1. 主流程覆盖是否完整。
                2. 异常流程是否充分。
                3. 边界场景是否覆盖。
                4. 数据一致性是否可验证。
                5. 用例是否可执行。
                6. 来源是否可追溯。
                """;
    }

    private List<SkillDefinition> buildBuiltinSkills() {
        List<SkillDefinition> skills = new ArrayList<>();

        skills.add(buildSkill(
                "FUNCTIONAL_TEST",
                "功能测试 Skill",
                "TEST_DESIGN",
                "用于生成业务正常流程、状态流转和核心功能路径测试用例。",
                """
                        请重点从功能测试角度设计用例：
                        1. 覆盖用户正常主流程。
                        2. 覆盖关键业务状态变化。
                        3. 覆盖前置条件、操作步骤、预期结果。
                        4. 覆盖前后端数据一致性。
                        5. 对核心 P0 流程给出最高优先级。
                        """,
                """
                        {
                          "requiredFields": [
                            "caseTitle",
                            "caseType",
                            "priority",
                            "precondition",
                            "steps",
                            "expectedResult"
                          ]
                        }
                        """,
                """
                        [
                          {
                            "dimension": "主流程覆盖",
                            "maxScore": 20,
                            "rule": "是否覆盖需求中的核心成功路径和关键状态变化"
                          },
                          {
                            "dimension": "业务规则覆盖",
                            "maxScore": 15,
                            "rule": "是否覆盖 PRD 中明确描述的业务规则"
                          }
                        ]
                        """
        ));

        skills.add(buildSkill(
                "EXCEPTION_TEST",
                "异常测试 Skill",
                "TEST_DESIGN",
                "用于生成参数异常、状态异常、服务失败、流程中断等异常场景用例。",
                """
                        请重点从异常测试角度设计用例：
                        1. 覆盖用户未登录、无权限、状态不满足等异常。
                        2. 覆盖接口失败、支付失败、服务异常等场景。
                        3. 覆盖流程中断、超时、取消、失败重试等场景。
                        4. 每条异常用例必须说明系统不应该发生什么错误结果。
                        """,
                """
                        {
                          "requiredFields": [
                            "caseTitle",
                            "caseType",
                            "priority",
                            "steps",
                            "expectedResult",
                            "riskPoint"
                          ]
                        }
                        """,
                """
                        [
                          {
                            "dimension": "异常流程覆盖",
                            "maxScore": 20,
                            "rule": "是否覆盖失败、取消、超时、无权限、状态不满足等异常路径"
                          },
                          {
                            "dimension": "风险说明",
                            "maxScore": 10,
                            "rule": "是否说明异常场景可能导致的业务风险"
                          }
                        ]
                        """
        ));

        skills.add(buildSkill(
                "BOUNDARY_TEST",
                "边界测试 Skill",
                "TEST_DESIGN",
                "用于生成边界值、重复提交、频率限制、并发、弱网等测试用例。",
                """
                        请重点从边界测试角度设计用例：
                        1. 覆盖空值、超长、最小值、最大值、最大值+1 等输入边界。
                        2. 覆盖重复点击、重复提交、短时间频繁操作。
                        3. 覆盖弱网、超时、并发请求、幂等性。
                        4. 对可能造成资损或数据错乱的边界场景标记为 P0 或 P1。
                        """,
                """
                        {
                          "boundaryTypes": [
                            "EMPTY",
                            "MAX",
                            "MIN",
                            "REPEAT_SUBMIT",
                            "CONCURRENT",
                            "WEAK_NETWORK"
                          ]
                        }
                        """,
                """
                        [
                          {
                            "dimension": "边界场景覆盖",
                            "maxScore": 15,
                            "rule": "是否覆盖输入边界、频率边界、并发边界和弱网边界"
                          }
                        ]
                        """
        ));

        skills.add(buildSkill(
                "DATA_CONSISTENCY_TEST",
                "数据一致性测试 Skill",
                "TEST_DESIGN",
                "用于生成数据库、缓存、订单状态、权益状态等一致性校验用例。",
                """
                        请重点从数据一致性角度设计用例：
                        1. 覆盖前端展示状态和后端数据状态是否一致。
                        2. 覆盖订单状态、支付状态、权益状态是否一致。
                        3. 覆盖数据库、缓存、搜索索引、消息队列最终一致性。
                        4. 每条用例尽量给出可自动化断言建议。
                        """,
                """
                        {
                          "consistencyObjects": [
                            "DATABASE",
                            "CACHE",
                            "ORDER_STATUS",
                            "RIGHTS_STATUS",
                            "MESSAGE_EVENT"
                          ]
                        }
                        """,
                """
                        [
                          {
                            "dimension": "数据一致性",
                            "maxScore": 15,
                            "rule": "是否覆盖状态同步、数据落库、缓存更新和最终一致性验证"
                          },
                          {
                            "dimension": "自动化可验证性",
                            "maxScore": 10,
                            "rule": "是否给出接口、数据库或日志层面的自动化断言建议"
                          }
                        ]
                        """
        ));

        skills.add(buildSkill(
                "REGRESSION_TEST",
                "回归测试 Skill",
                "REGRESSION",
                "用于根据新需求影响范围生成旧功能回归测试用例。",
                """
                        请重点从回归测试角度设计用例：
                        1. 分析新需求可能影响的旧功能。
                        2. 生成需要回归的历史主流程用例。
                        3. 标记每条回归用例的回归目的。
                        4. 不要简单重复历史用例，要说明为什么本次变更需要回归。
                        """,
                """
                        {
                          "regressionFields": [
                            "affectedModule",
                            "regressionReason",
                            "historicalCaseReference"
                          ]
                        }
                        """,
                """
                        [
                          {
                            "dimension": "回归范围覆盖",
                            "maxScore": 20,
                            "rule": "是否覆盖新需求影响到的旧模块、旧流程、历史高风险用例"
                          }
                        ]
                        """
        ));

        skills.add(buildSkill(
                "AI_APP_TEST",
                "AI 应用测试 Skill",
                "AI_APP_TEST",
                "用于生成 LLM、RAG、Agent、Prompt 注入、幻觉、知识越权等 AI 应用测试用例。",
                """
                        请重点从 AI 应用测试角度设计用例：
                        1. 覆盖 LLM 输出准确性、一致性、稳定性。
                        2. 覆盖 RAG 召回准确性、来源引用、无答案拒答。
                        3. 覆盖 Prompt 注入、知识越权、上下文污染。
                        4. 覆盖 Agent 工具调用正确性、步骤可恢复性、失败重试。
                        5. 覆盖模型超时、降级、限流、成本控制和可观测性。
                        """,
                """
                        {
                          "aiTestDimensions": [
                            "ACCURACY",
                            "HALLUCINATION",
                            "PROMPT_INJECTION",
                            "KNOWLEDGE_ACCESS_CONTROL",
                            "RAG_RETRIEVAL",
                            "AGENT_TOOL_CALL",
                            "STABILITY",
                            "COST"
                          ]
                        }
                        """,
                """
                        [
                          {
                            "dimension": "AI 应用风险覆盖",
                            "maxScore": 20,
                            "rule": "是否覆盖幻觉、注入、越权、召回错误、工具误调用等 AI 应用风险"
                          }
                        ]
                        """
        ));

        return skills;
    }

    private SkillDefinition buildSkill(
            String code,
            String name,
            String type,
            String description,
            String promptTemplate,
            String outputSchema,
            String rubric
    ) {
        SkillDefinition skill = new SkillDefinition();
        skill.setSkillCode(code);
        skill.setSkillName(name);
        skill.setSkillType(type);
        skill.setDescription(description);
        skill.setPromptTemplate(promptTemplate);
        skill.setOutputSchema(outputSchema);
        skill.setRubric(rubric);
        skill.setVersionNo("1.0.0");
        skill.setEnabled(1);
        return skill;
    }

    private SkillDefinitionVO toVO(SkillDefinition skill) {
        SkillDefinitionVO vo = new SkillDefinitionVO();

        vo.setId(skill.getId());
        vo.setSkillCode(skill.getSkillCode());
        vo.setSkillName(skill.getSkillName());
        vo.setSkillType(skill.getSkillType());
        vo.setDescription(skill.getDescription());
        vo.setPromptTemplate(skill.getPromptTemplate());
        vo.setOutputSchema(skill.getOutputSchema());
        vo.setRubric(skill.getRubric());
        vo.setVersionNo(skill.getVersionNo());
        vo.setEnabled(skill.getEnabled());
        vo.setCreateTime(skill.getCreateTime());
        vo.setUpdateTime(skill.getUpdateTime());

        return vo;
    }
}