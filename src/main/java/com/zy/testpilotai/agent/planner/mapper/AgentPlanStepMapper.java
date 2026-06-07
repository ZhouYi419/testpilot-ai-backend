package com.zy.testpilotai.agent.planner.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zy.testpilotai.agent.planner.model.entity.AgentPlanStep;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AgentPlanStepMapper extends BaseMapper<AgentPlanStep> {
}