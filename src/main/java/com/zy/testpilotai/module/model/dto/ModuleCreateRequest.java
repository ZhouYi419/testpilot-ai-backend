package com.zy.testpilotai.module.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ModuleCreateRequest {

    /**
     * 归属的项目ID
     */
    @NotNull(message = "项目ID不能为空")
    private Long projectId;

    /**
     * 模块编码
     */
    @NotBlank(message = "模块编码不能为空")
    private String moduleCode;

    /**
     * 模块名称
     */
    @NotBlank(message = "模块名称不能为空")
    private String moduleName;

    /**
     * 父模块ID
     */
    private Long parentModuleId;

    /**
     * 模块描述
     */
    private String description;
}