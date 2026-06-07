package com.zy.testpilotai.agent.planner.tool;

import com.zy.testpilotai.agent.planner.model.vo.AgentToolInfoVO;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AgentToolRegistry {

    private final Map<String, AgentToolExecutor> executorMap = new LinkedHashMap<>();

    public AgentToolRegistry(List<AgentToolExecutor> executors) {
        for (AgentToolExecutor executor : executors) {
            executorMap.put(executor.toolName(), executor);
        }
    }

    public AgentToolExecutor get(String toolName) {
        AgentToolExecutor executor = executorMap.get(toolName);

        if (executor == null) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "未注册的 Agent 工具：" + toolName
            );
        }

        return executor;
    }

    public boolean contains(String toolName) {
        return executorMap.containsKey(toolName);
    }

    public List<String> toolNames() {
        return executorMap.keySet().stream().toList();
    }

    public List<AgentToolInfoVO> listTools() {
        return executorMap.values()
                .stream()
                .map(executor -> {
                    AgentToolInfoVO vo = new AgentToolInfoVO();
                    vo.setToolName(executor.toolName());
                    vo.setDescription(executor.description());
                    vo.setRequiredParams(executor.requiredParams());
                    return vo;
                })
                .toList();
    }
}