package com.zy.testpilotai.project.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProjectVO {

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
     * 状态
     */
    private Integer status;

    private LocalDateTime createTime;
}