package com.zy.testpilotai.project.model.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 项目实体
 */
@Data
public class ProjectEntity {

    private Long id;

    private String name;

    private String description;

    private String ownerName;

    /**
     * 1 启用，0 停用
     */
    private Integer status;

    /**
     * 是否删除
     */
    private Boolean deleted;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}