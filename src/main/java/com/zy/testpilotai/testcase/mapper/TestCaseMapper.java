package com.zy.testpilotai.testcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zy.testpilotai.testcase.model.entity.TestCase;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestCaseMapper extends BaseMapper<TestCase> {
}