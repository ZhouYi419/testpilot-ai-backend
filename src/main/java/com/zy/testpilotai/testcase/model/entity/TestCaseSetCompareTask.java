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
@TableName(value = "test_case_set_compare_task", autoResultMap = true)
public class TestCaseSetCompareTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 对比任务业务 ID。
     */
    private String compareTaskId;

    /**
     * 项目 ID。
     */
    private Long projectId;

    /**
     * 源用例集 ID。
     */
    private String sourceCaseSetId;

    /**
     * 目标用例集 ID。
     */
    private String targetCaseSetId;

    /**
     * 状态：RUNNING / SUCCESS / FAILED。
     */
    private String status;

    /**
     * 新增数量。
     */
    private Integer addedCount;

    /**
     * 删除数量。
     */
    private Integer removedCount;

    /**
     * 修改数量。
     */
    private Integer modifiedCount;

    /**
     * 未变化数量。
     */
    private Integer unchangedCount;

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