package com.zy.testpilotai.module.service;

import com.zy.testpilotai.module.model.dto.ModuleCreateRequest;
import com.zy.testpilotai.module.model.vo.ProjectModuleVO;
import java.util.List;

public interface ProjectModuleService {

    /**
     * 创建模块
     */
    ProjectModuleVO create(ModuleCreateRequest request);

    /**
     * 根据项目id查询模块列表
     */
    List<ProjectModuleVO> listByProjectId(Long projectId);
}