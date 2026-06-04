package com.zy.testpilotai.skill.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SkillCreateRequest {
    /**
     * Skill 编码。
     */
    @NotBlank(message = "Skill编码不能为空")
    private String skillCode;

    /**
     * Skill 名称。
     */
    @NotBlank(message = "Skill名称不能为空")
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
     * 生成用例时使用的 Prompt 模板。
     */
    @NotBlank(message = "Skill Prompt模板不能为空")
    private String promptTemplate;

    /**
     * 输出结构约束 JSON 字符串。
     */
    private String outputSchema;

    /**
     * 质量评分规则 JSON 字符串。
     */
    private String rubric;

    /**
     * 版本号。
     */
    private String versionNo = "1.0.0";
}