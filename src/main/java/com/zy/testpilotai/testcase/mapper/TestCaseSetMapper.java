package com.zy.testpilotai.testcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zy.testpilotai.testcase.model.entity.TestCaseSet;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestCaseSetMapper extends BaseMapper<TestCaseSet> {
}