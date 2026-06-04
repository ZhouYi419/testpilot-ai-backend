package com.zy.testpilotai.module.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project_module")
public class ProjectModule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 归属的项目ID，关联 project 表的 id
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
     * 模块状态
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}