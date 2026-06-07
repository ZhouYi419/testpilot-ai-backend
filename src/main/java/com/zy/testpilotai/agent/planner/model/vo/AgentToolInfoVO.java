package com.zy.testpilotai.agent.planner.model.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AgentToolInfoVO {

    /**
     * 工具名称。
     */
    private String toolName;

    /**
     * 工具描述。
     */
    private String description;

    /**
     * 必填参数。
     */
    private List<String> requiredParams = new ArrayList<>();
}