package com.zy.testpilotai.module.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.module.mapper.ProjectModuleMapper;
import com.zy.testpilotai.module.model.dto.ModuleCreateRequest;
import com.zy.testpilotai.module.model.entity.ProjectModule;
import com.zy.testpilotai.module.model.vo.ProjectModuleVO;
import com.zy.testpilotai.module.service.ProjectModuleService;
import com.zy.testpilotai.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectModuleServiceImpl implements ProjectModuleService {

    private final ProjectModuleMapper projectModuleMapper;

    private final ProjectService projectService;

    @Override
    public ProjectModuleVO create(ModuleCreateRequest request) {
        projectService.getById(request.getProjectId());

        Long count = projectModuleMapper.selectCount(
                new LambdaQueryWrapper<ProjectModule>()
                        .eq(ProjectModule::getProjectId, request.getProjectId())
                        .eq(ProjectModule::getModuleCode, request.getModuleCode())
        );

        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "该项目下模块编码已存在");
        }

        ProjectModule module = new ProjectModule();
        module.setProjectId(request.getProjectId());
        module.setModuleCode(request.getModuleCode());
        module.setModuleName(request.getModuleName());
        module.setParentModuleId(request.getParentModuleId());
        module.setDescription(request.getDescription());
        module.setStatus(1);
        module.setCreateTime(LocalDateTime.now());
        module.setUpdateTime(LocalDateTime.now());

        projectModuleMapper.insert(module);

        return toVO(module);
    }

    @Override
    public List<ProjectModuleVO> listByProjectId(Long projectId) {
        projectService.getById(projectId);

        List<ProjectModule> modules = projectModuleMapper.selectList(
                new LambdaQueryWrapper<ProjectModule>()
                        .eq(ProjectModule::getProjectId, projectId)
                        .eq(ProjectModule::getStatus, 1)
                        .orderByAsc(ProjectModule::getId)
        );

        return modules.stream().map(this::toVO).toList();
    }

    private ProjectModuleVO toVO(ProjectModule module) {
        ProjectModuleVO vo = new ProjectModuleVO();
        vo.setId(module.getId());
        vo.setProjectId(module.getProjectId());
        vo.setModuleCode(module.getModuleCode());
        vo.setModuleName(module.getModuleName());
        vo.setParentModuleId(module.getParentModuleId());
        vo.setDescription(module.getDescription());
        vo.setCreateTime(module.getCreateTime());
        return vo;
    }
}