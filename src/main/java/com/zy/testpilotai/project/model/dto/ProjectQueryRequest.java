package com.zy.testpilotai.project.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 项目分页查询请求
 */
@Data
public class ProjectQueryRequest {

    /**
     * 当前页
     */
    @Min(value = 1, message = "pageNum 不能小于1")
    private Integer pageNum = 1;

    /**
     * 每页大小
     */
    @Min(value = 1, message = "pageSize 不能小于1")
    @Max(value = 100, message = "pageSize 不能大于100")
    private Integer pageSize = 10;

    /**
     * 项目名称关键词
     */
    private String keyword;
}