package com.zy.testpilotai.testcase.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("test_case_set")
public class TestCaseSet {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用例集业务 ID。
     */
    private String caseSetId;

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
     * 用例集名称。
     */
    private String setName;

    /**
     * 用例集类型：
     * FULL / INCREMENTAL / REGRESSION / AI_APP / CUSTOM。
     */
    private String setType;

    /**
     * 用例集描述。
     */
    private String description;

    /**
     * 用例数量。
     */
    private Integer caseCount;

    /**
     * 状态：
     * ACTIVE / DELETED。
     */
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}