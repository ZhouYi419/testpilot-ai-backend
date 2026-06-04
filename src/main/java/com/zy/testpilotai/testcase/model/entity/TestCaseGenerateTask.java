package com.zy.testpilotai.testcase.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.LocalDateTime;

@Data
@TableName(value = "testcase_generate_task", autoResultMap = true)
public class TestCaseGenerateTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 业务任务 ID
     */
    private String taskId;

    /**
     * 项目 ID
     */
    private Long projectId;

    /**
     * 版本号
     */
    private String versionNo;

    /**
     * 模块编码
     */
    private String moduleCode;

    /**
     * 生成目标
     */
    private String generateGoal;

    /**
     * 生成类型
     * FULL：全量生成
     * MODULE：按模块生成
     * INCREMENTAL：增量生成，后续再扩展
     */
    private String generateType;

    /**
     * 选择的 Skill 列表，JSONB 存储
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String selectedSkills;

    /**
     * 任务状态
     * PENDING / RUNNING / SUCCESS / FAILED
     */
    private String status;

    /**
     * 模型原始输出
     */
    private String rawModelOutput;

    /**
     * 质量评分
     * 第一版先不做评分，后续 Step 6 扩展
     */
    private Double qualityScore;

    /**
     * 错误信息
     */
    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}