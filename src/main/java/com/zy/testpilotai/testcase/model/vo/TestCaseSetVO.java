package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TestCaseSetVO {

    private Long id;

    /**
     * 用例集业务编号
     */
    private String caseSetId;

    /**
     * 归属的项目ID
     */
    private Long projectId;

    /**
     * 关联的文档版本号
     */
    private String versionNo;

    /**
     * 关联的模块编码
     */
    private String moduleCode;

    /**
     * 用例集名称
     */
    private String setName;

    /**
     * 用例集类型
     */
    private String setType;

    /**
     * 用例集描述
     */
    private String description;

    /**
     * 包含的测试用例数量
     */
    private Integer caseCount;

    /**
     * 用例集状态
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}