package com.zy.testpilotai.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zy.testpilotai.project.model.entity.Project;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
}