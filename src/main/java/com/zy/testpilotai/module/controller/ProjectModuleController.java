package com.zy.testpilotai.module.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.module.model.dto.ModuleCreateRequest;
import com.zy.testpilotai.module.model.vo.ProjectModuleVO;
import com.zy.testpilotai.module.service.ProjectModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ProjectModuleController {

    private final ProjectModuleService projectModuleService;

    /**
     * 创建模块
     */
    @PostMapping("/create")
    public BaseResponse<ProjectModuleVO> create(@RequestBody @Valid ModuleCreateRequest request) {
        return ResultUtils.success(projectModuleService.create(request));
    }

    /**
     * 获取模块列表
     */
    @GetMapping("/list")
    public BaseResponse<List<ProjectModuleVO>> list(@RequestParam Long projectId) {
        return ResultUtils.success(projectModuleService.listByProjectId(projectId));
    }
}