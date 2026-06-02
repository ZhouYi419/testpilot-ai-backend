package com.zy.testpilotai.project.mapper;

import com.zy.testpilotai.project.model.entity.ProjectEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProjectMapper {

    int insert(ProjectEntity project);

    ProjectEntity selectById(@Param("id") Long id);

    List<ProjectEntity> selectPage(
            @Param("keyword") String keyword,
            @Param("offset") Integer offset,
            @Param("pageSize") Integer pageSize
    );

    long countPage(@Param("keyword") String keyword);

    int updateById(ProjectEntity project);

    int logicDeleteById(@Param("id") Long id);
}