package com.zy.testpilotai.skill.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.LocalDateTime;

@Data
@TableName(value = "skill_definition", autoResultMap = true)
public class SkillDefinition {

    @TableId(type = IdType.AUTO)
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
     * Skill 描述。
     */
    private String description;

    /**
     * 生成测试用例时使用的 Prompt 模板。
     */
    private String promptTemplate;

    /**
     * 输出结构约束。
     * 使用 PostgreSQL JSONB 存储。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String outputSchema;

    /**
     * 质量评分规则。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String rubric;

    /**
     * Skill 版本号。
     */
    private String versionNo;

    /**
     * 是否启用。
     * 1：启用
     * 0：禁用
     */
    private Integer enabled;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}