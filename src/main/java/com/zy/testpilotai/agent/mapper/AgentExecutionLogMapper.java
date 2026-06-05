package com.zy.testpilotai.agent.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zy.testpilotai.agent.model.entity.AgentExecutionLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentExecutionLogMapper extends BaseMapper<AgentExecutionLog> {
}