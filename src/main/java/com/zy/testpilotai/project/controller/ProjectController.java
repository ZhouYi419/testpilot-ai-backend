package com.zy.testpilotai.project.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.project.model.dto.ProjectCreateRequest;
import com.zy.testpilotai.project.model.vo.ProjectVO;
import com.zy.testpilotai.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 创建项目
     */
    @PostMapping("/create")
    public BaseResponse<ProjectVO> create(@RequestBody @Valid ProjectCreateRequest request) {
        return ResultUtils.success(projectService.create(request));
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/{id}")
    public BaseResponse<ProjectVO> getById(@PathVariable Long id) {
        return ResultUtils.success(projectService.getById(id));
    }

    /**
     * 获取项目列表
     */
    @GetMapping("/list")
    public BaseResponse<List<ProjectVO>> list() {
        return ResultUtils.success(projectService.list());
    }
}