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
@TableName(value = "testcase_quality_review_task", autoResultMap = true)
public class TestCaseQualityReviewTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 质量评审任务 ID
     */
    private String reviewTaskId;

    /**
     * 被评审的测试用例生成任务 ID
     */
    private String sourceTaskId;

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
     * 评审任务状态
     */
    private String status;

    /**
     * 总评分
     */
    private Double totalScore;

    /**
     * 完整评审结果 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String reviewResult;

    /**
     * 缺失测试点 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String missingPoints;

    /**
     * 建议补全方向 JSON
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String suggestedCaseDirections;

    /**
     * 模型原始输出
     */
    private String rawModelOutput;

    /**
     * 错误信息
     */
    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}