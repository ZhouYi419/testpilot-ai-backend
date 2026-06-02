package com.zy.testpilotai.project.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 项目展示对象
 */
@Data
public class ProjectVO {

    private Long id;

    private String name;

    private String description;

    private String ownerName;

    private Integer status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}