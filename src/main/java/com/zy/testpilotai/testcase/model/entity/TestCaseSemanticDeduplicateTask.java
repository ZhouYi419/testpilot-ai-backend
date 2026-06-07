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
@TableName(value = "test_case_semantic_deduplicate_task", autoResultMap = true)
public class TestCaseSemanticDeduplicateTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 语义去重任务业务 ID。
     */
    private String deduplicateTaskId;

    /**
     * 对比范围：
     * TASK / VERSION / PROJECT / CROSS_VERSION。
     */
    private String compareScope;

    /**
     * 源任务 ID。
     */
    private String taskId;

    /**
     * 项目 ID。
     */
    private Long projectId;

    /**
     * 版本号。
     */
    private String versionNo;

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 相似度阈值。
     */
    private Double threshold;

    /**
     * 每条用例最多返回候选数量。
     */
    private Integer topK;

    /**
     * 是否重建向量：
     * 1：是
     * 0：否。
     */
    private Integer rebuildEmbedding;

    /**
     * 状态：RUNNING / SUCCESS / FAILED。
     */
    private String status;

    /**
     * 源用例数量。
     */
    private Integer sourceCaseCount;

    /**
     * 候选用例数量。
     */
    private Integer candidateCaseCount;

    /**
     * 重复对数量。
     */
    private Integer duplicatePairCount;

    /**
     * 被标记重复的用例数量。
     */
    private Integer markedDuplicateCount;

    /**
     * 汇总 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String summary;

    /**
     * 错误信息。
     */
    private String errorMessage;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}