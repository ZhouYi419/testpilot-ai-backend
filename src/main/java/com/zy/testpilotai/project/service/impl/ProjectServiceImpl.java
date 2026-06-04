package com.zy.testpilotai.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.project.mapper.ProjectMapper;
import com.zy.testpilotai.project.model.dto.ProjectCreateRequest;
import com.zy.testpilotai.project.model.entity.Project;
import com.zy.testpilotai.project.model.vo.ProjectVO;
import com.zy.testpilotai.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    @Override
    public ProjectVO create(ProjectCreateRequest request) {
        Project project = new Project();
        project.setProjectName(request.getProjectName());
        project.setDescription(request.getDescription());
        project.setOwnerId(0L);
        project.setStatus(1);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());

        projectMapper.insert(project);

        return toVO(project);
    }

    @Override
    public ProjectVO getById(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "项目不存在");
        }
        return toVO(project);
    }

    @Override
    public List<ProjectVO> list() {
        List<Project> projects = projectMapper.selectList(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getStatus, 1)
                        .orderByDesc(Project::getCreateTime)
        );
        return projects.stream().map(this::toVO).toList();
    }

    private ProjectVO toVO(Project project) {
        ProjectVO vo = new ProjectVO();
        vo.setId(project.getId());
        vo.setProjectName(project.getProjectName());
        vo.setDescription(project.getDescription());
        vo.setStatus(project.getStatus());
        vo.setCreateTime(project.getCreateTime());
        return vo;
    }
}