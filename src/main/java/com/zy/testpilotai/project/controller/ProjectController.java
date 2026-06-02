package com.zy.testpilotai.project.controller;

import com.zy.testpilotai.common.model.PageResult;
import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.project.model.dto.ProjectCreateRequest;
import com.zy.testpilotai.project.model.dto.ProjectQueryRequest;
import com.zy.testpilotai.project.model.dto.ProjectUpdateRequest;
import com.zy.testpilotai.project.model.vo.ProjectVO;
import com.zy.testpilotai.project.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    /**
     * 创建项目
     */
    @PostMapping
    public BaseResponse<Long> createProject(@Valid @RequestBody ProjectCreateRequest request) {
        return ResultUtils.success(projectService.createProject(request));
    }

    /**
     * 分页查询项目
     */
    @GetMapping
    public BaseResponse<PageResult<ProjectVO>> pageProjects(@Valid ProjectQueryRequest request) {
        return ResultUtils.success(projectService.pageProjects(request));
    }

    /**
     * 查询项目详情
     */
    @GetMapping("/{id}")
    public BaseResponse<ProjectVO> getProjectById(@PathVariable Long id) {
        return ResultUtils.success(projectService.getProjectById(id));
    }

    /**
     * 更新项目
     */
    @PutMapping("/{id}")
    public BaseResponse<Boolean> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectUpdateRequest request
    ) {
        request.setId(id);
        return ResultUtils.success(projectService.updateProject(request));
    }

    /**
     * 删除项目
     */
    @DeleteMapping("/{id}")
    public BaseResponse<Boolean> deleteProject(@PathVariable Long id) {
        return ResultUtils.success(projectService.deleteProject(id));
    }
}