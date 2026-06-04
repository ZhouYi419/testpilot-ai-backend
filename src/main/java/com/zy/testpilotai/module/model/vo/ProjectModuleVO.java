package com.zy.testpilotai.module.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectModuleVO {

    private Long id;

    /**
     * 归属的项目ID
     */
    private Long projectId;

    /**
     * 模块编码
     */
    private String moduleCode;

    /**
     * 模块名称
     */
    private String moduleName;

    /**
     * 父模块ID
     */
    private Long parentModuleId;

    /**
     * 模块描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}