package com.zy.testpilotai.project.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("project")
public class Project {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目描述
     */
    private String description;

    /**
     * 创建人/负责人ID
     */
    private Long ownerId;

    /**
     * 项目状态
     */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}