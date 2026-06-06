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
@TableName(value = "test_case_set_compare_result", autoResultMap = true)
public class TestCaseSetCompareResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 对比任务业务 ID。
     */
    private String compareTaskId;

    /**
     * 结果类型：
     * ADDED / REMOVED / MODIFIED / UNCHANGED。
     */
    private String resultType;

    /**
     * 源用例 ID。
     */
    private Long sourceTestCaseId;

    /**
     * 目标用例 ID。
     */
    private Long targetTestCaseId;

    /**
     * 源用例标题。
     */
    private String sourceCaseTitle;

    /**
     * 目标用例标题。
     */
    private String targetCaseTitle;

    /**
     * 变更说明。
     */
    private String changeSummary;

    /**
     * 字段差异 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String fieldDiffs;

    private LocalDateTime createTime;
}