package com.zy.testpilotai.project.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新项目请求
 */
@Data
public class ProjectUpdateRequest {

    @NotNull(message = "项目ID不能为空")
    private Long id;

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称不能超过100个字符")
    private String name;

    @Size(max = 2000, message = "项目描述不能超过2000个字符")
    private String description;

    @Size(max = 100, message = "负责人名称不能超过100个字符")
    private String ownerName;

    /**
     * 1 启用，0 停用
     */
    private Integer status;
}