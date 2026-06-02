package com.zy.testpilotai.testcase.mapper;

import com.zy.testpilotai.testcase.model.entity.TestcaseGenerationTaskEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TestcaseGenerationTaskMapper {

    int insert(TestcaseGenerationTaskEntity task);

    TestcaseGenerationTaskEntity selectById(@Param("id") Long id);

    int updateStatus(
            @Param("id") Long id,
            @Param("status") String status,
            @Param("totalCases") Integer totalCases,
            @Param("modelName") String modelName,
            @Param("errorMessage") String errorMessage
    );
}