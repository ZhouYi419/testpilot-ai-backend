package com.zy.testpilotai.agent.planner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zy.testpilotai.agent.planner.model.entity.AgentPlanTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentPlanTaskMapper extends BaseMapper<AgentPlanTask> {
}