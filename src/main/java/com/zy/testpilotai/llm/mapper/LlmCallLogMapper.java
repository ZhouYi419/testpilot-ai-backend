package com.zy.testpilotai.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zy.testpilotai.llm.model.entity.LlmCallLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LlmCallLogMapper extends BaseMapper<LlmCallLog> {
}