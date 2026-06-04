package com.zy.testpilotai.skill.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SkillUpdateRequest {

    /**
     * Skill ID。
     */
    @NotNull(message = "Skill ID不能为空")
    private Long id;

    /**
     * Skill 名称。
     */
    private String skillName;

    /**
     * Skill 类型。
     */
    private String skillType;

    /**
     * Skill 描述。
     */
    private String description;

    /**
     * Prompt 模板。
     */
    private String promptTemplate;

    /**
     * 输出结构约束。
     */
    private String outputSchema;

    /**
     * 评分规则。
     */
    private String rubric;

    /**
     * 版本号。
     */
    private String versionNo;
}