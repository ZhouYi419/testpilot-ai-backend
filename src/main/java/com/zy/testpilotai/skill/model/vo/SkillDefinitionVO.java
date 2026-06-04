package com.zy.testpilotai.skill.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SkillDefinitionVO {

    /**
     * Skill ID。
     */
    private Long id;

    /**
     * Skill 编码。
     */
    private String skillCode;

    /**
     * Skill 名称。
     */
    private String skillName;

    /**
     * Skill 类型。
     */
    private String skillType;

    /**
     * 描述。
     */
    private String description;

    /**
     * Prompt 模板。
     */
    private String promptTemplate;

    /**
     * 输出结构约束 JSON。
     */
    private String outputSchema;

    /**
     * 评分规则 JSON。
     */
    private String rubric;

    /**
     * 版本号。
     */
    private String versionNo;

    /**
     * 是否启用。
     */
    private Integer enabled;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}