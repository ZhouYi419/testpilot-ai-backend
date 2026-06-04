package com.zy.testpilotai.skill.service;

import com.zy.testpilotai.skill.model.dto.SkillCreateRequest;
import com.zy.testpilotai.skill.model.dto.SkillUpdateRequest;
import com.zy.testpilotai.skill.model.vo.SkillDefinitionVO;
import java.util.List;

public interface SkillDefinitionService {

    /**
     * 初始化内置 Skill
     */
    List<SkillDefinitionVO> initBuiltinSkills();

    /**
     * 创建自定义 Skill
     */
    SkillDefinitionVO create(SkillCreateRequest request);

    /**
     * 更新 Skill
     */
    SkillDefinitionVO update(SkillUpdateRequest request);

    /**
     * 查询 Skill 列表
     */
    List<SkillDefinitionVO> list(Boolean enabledOnly);

    /**
     * 根据 Skill 编码查询详情
     */
    SkillDefinitionVO getByCode(String skillCode);

    /**
     * 启用 Skill
     */
    Boolean enable(String skillCode);

    /**
     * 禁用 Skill
     */
    Boolean disable(String skillCode);

    /**
     * 根据 selectedSkills 构建测试用例生成 Prompt 片段
     */
    String buildGenerationSkillPrompt(List<String> selectedSkills);

    /**
     * 根据 selectedSkills 构建质量评审 Rubric 片段
     */
    String buildReviewRubricPrompt(List<String> selectedSkills);

    /**
     * selectedSkills 在任务表中是 JSON 字符串。
     * 这个方法用于从 JSON 字符串构建生成 Prompt
     */
    String buildGenerationSkillPromptFromJson(String selectedSkillsJson);

    /**
     * selectedSkills 在任务表中是 JSON 字符串。
     * 这个方法用于从 JSON 字符串构建评审 Rubric。
     */
    String buildReviewRubricPromptFromJson(String selectedSkillsJson);
}