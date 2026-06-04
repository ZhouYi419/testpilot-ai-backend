package com.zy.testpilotai.project.service;

import com.zy.testpilotai.project.model.dto.ProjectCreateRequest;
import com.zy.testpilotai.project.model.vo.ProjectVO;
import java.util.List;

public interface ProjectService {

    /**
     * 创建项目
     */
    ProjectVO create(ProjectCreateRequest request);

    /**
     * 根据id查询项目
     */
    ProjectVO getById(Long id);

    /**
     * 获取项目列表
     */
    List<ProjectVO> list();
}