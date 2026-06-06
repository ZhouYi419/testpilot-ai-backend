package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestCaseSetCompareTaskVO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 对比任务业务编号
     */
    private String compareTaskId;

    /**
     * 归属的项目ID
     */
    private Long projectId;

    /**
     * 源用例集ID (Source)
     */
    private String sourceCaseSetId;

    /**
     * 目标用例集ID (Target)
     */
    private String targetCaseSetId;

    /**
     * 任务执行状态
     */
    private String status;

    /**
     * 新增的用例数量统计
     */
    private Integer addedCount;

    /**
     * 删除/废弃的用例数量统计
     */
    private Integer removedCount;

    /**
     * 修改的用例数量统计
     */
    private Integer modifiedCount;

    /**
     * 未发生变化的用例数量统计
     */
    private Integer unchangedCount;

    /**
     * 对比总结/分析报告
     */
    private String summary;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;

    /**
     * 任务更新/完成时间
     */
    private LocalDateTime updateTime;
}