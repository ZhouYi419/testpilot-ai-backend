package com.zy.testpilotai.project.service.impl;

import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.common.model.PageResult;
import com.zy.testpilotai.project.mapper.ProjectMapper;
import com.zy.testpilotai.project.model.dto.ProjectCreateRequest;
import com.zy.testpilotai.project.model.dto.ProjectQueryRequest;
import com.zy.testpilotai.project.model.dto.ProjectUpdateRequest;
import com.zy.testpilotai.project.model.entity.ProjectEntity;
import com.zy.testpilotai.project.model.vo.ProjectVO;
import com.zy.testpilotai.project.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

/**
 * 项目服务实现
 */
@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;

    @Override
    public Long createProject(ProjectCreateRequest request) {
        if (request == null || !StringUtils.hasText(request.getName())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "项目名称不能为空");
        }

        ProjectEntity project = new ProjectEntity();
        project.setName(request.getName().trim());
        project.setDescription(request.getDescription());
        project.setOwnerName(request.getOwnerName());
        project.setStatus(1);

        int rows = projectMapper.insert(project);
        if (rows <= 0 || project.getId() == null) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "创建项目失败");
        }

        return project.getId();
    }

    @Override
    public ProjectVO getProjectById(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "项目ID不合法");
        }

        ProjectEntity project = projectMapper.selectById(id);
        if (project == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "项目不存在");
        }

        return toVO(project);
    }

    @Override
    public PageResult<ProjectVO> pageProjects(ProjectQueryRequest request) {
        if (request == null) {
            request = new ProjectQueryRequest();
        }

        Integer pageNum = request.getPageNum() == null ? 1 : request.getPageNum();
        Integer pageSize = request.getPageSize() == null ? 10 : request.getPageSize();

        if (pageNum < 1) {
            pageNum = 1;
        }

        if (pageSize < 1) {
            pageSize = 10;
        }

        if (pageSize > 100) {
            pageSize = 100;
        }

        int offset = (pageNum - 1) * pageSize;
        String keyword = StringUtils.hasText(request.getKeyword()) ? request.getKeyword().trim() : null;

        long total = projectMapper.countPage(keyword);
        List<ProjectEntity> records = projectMapper.selectPage(keyword, offset, pageSize);

        List<ProjectVO> voList = records.stream()
                .map(this::toVO)
                .toList();

        return PageResult.of(pageNum, pageSize, total, voList);
    }

    @Override
    public Boolean updateProject(ProjectUpdateRequest request) {
        if (request == null || request.getId() == null || request.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "项目ID不合法");
        }

        ProjectEntity existProject = projectMapper.selectById(request.getId());
        if (existProject == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "项目不存在");
        }

        ProjectEntity project = new ProjectEntity();
        project.setId(request.getId());
        project.setName(request.getName().trim());
        project.setDescription(request.getDescription());
        project.setOwnerName(request.getOwnerName());
        project.setStatus(request.getStatus() == null ? existProject.getStatus() : request.getStatus());

        int rows = projectMapper.updateById(project);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "更新项目失败");
        }

        return true;
    }

    @Override
    public Boolean deleteProject(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "项目ID不合法");
        }

        ProjectEntity existProject = projectMapper.selectById(id);
        if (existProject == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "项目不存在");
        }

        int rows = projectMapper.logicDeleteById(id);
        if (rows <= 0) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "删除项目失败");
        }

        return true;
    }

    private ProjectVO toVO(ProjectEntity entity) {
        if (entity == null) {
            return null;
        }

        ProjectVO vo = new ProjectVO();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setOwnerName(entity.getOwnerName());
        vo.setStatus(entity.getStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}