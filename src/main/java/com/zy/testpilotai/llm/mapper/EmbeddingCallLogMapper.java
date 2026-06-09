package com.zy.testpilotai.llm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zy.testpilotai.llm.model.entity.EmbeddingCallLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface EmbeddingCallLogMapper extends BaseMapper<EmbeddingCallLog> {
}