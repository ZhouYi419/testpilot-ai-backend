package com.zy.testpilotai.project.service;

import com.zy.testpilotai.common.model.PageResult;
import com.zy.testpilotai.project.model.dto.ProjectCreateRequest;
import com.zy.testpilotai.project.model.dto.ProjectQueryRequest;
import com.zy.testpilotai.project.model.dto.ProjectUpdateRequest;
import com.zy.testpilotai.project.model.vo.ProjectVO;

public interface ProjectService {

    Long createProject(ProjectCreateRequest request);

    ProjectVO getProjectById(Long id);

    PageResult<ProjectVO> pageProjects(ProjectQueryRequest request);

    Boolean updateProject(ProjectUpdateRequest request);

    Boolean deleteProject(Long id);
}