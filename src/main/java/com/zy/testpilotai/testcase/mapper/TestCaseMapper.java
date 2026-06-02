package com.zy.testpilotai.testcase.mapper;

import com.zy.testpilotai.testcase.model.entity.TestCaseEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface TestCaseMapper {

    int insert(TestCaseEntity testCase);

    List<TestCaseEntity> selectByTaskId(@Param("taskId") Long taskId);

    List<TestCaseEntity> selectByProject(
            @Param("projectId") Long projectId,
            @Param("versionName") String versionName,
            @Param("moduleName") String moduleName
    );
}