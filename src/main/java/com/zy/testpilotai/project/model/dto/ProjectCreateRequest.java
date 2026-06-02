package com.zy.testpilotai.project.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建项目请求
 */
@Data
public class ProjectCreateRequest {

    @NotBlank(message = "项目名称不能为空")
    @Size(max = 100, message = "项目名称不能超过100个字符")
    private String name;

    @Size(max = 2000, message = "项目描述不能超过2000个字符")
    private String description;

    @Size(max = 100, message = "负责人名称不能超过100个字符")
    private String ownerName;
}