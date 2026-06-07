package com.zy.testpilotai.automation.model.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AutomationScriptDetailVO {

    /**
     * 脚本生成任务。
     */
    private AutomationScriptTaskVO task;

    /**
     * 生成的文件。
     */
    private List<AutomationScriptFileVO> files = new ArrayList<>();
}