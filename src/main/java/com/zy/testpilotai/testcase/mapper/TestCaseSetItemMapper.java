package com.zy.testpilotai.testcase.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zy.testpilotai.testcase.model.entity.TestCaseSetItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TestCaseSetItemMapper extends BaseMapper<TestCaseSetItem> {
}