package com.zy.testpilotai.automation.service;

import com.zy.testpilotai.automation.model.dto.AutomationRunCancelRequest;
import com.zy.testpilotai.automation.model.dto.AutomationRunQueryRequest;
import com.zy.testpilotai.automation.model.dto.AutomationRunStartRequest;
import com.zy.testpilotai.automation.model.vo.AutomationRunDetailVO;
import com.zy.testpilotai.automation.model.vo.AutomationRunTaskVO;
import java.util.List;

public interface AutomationRunService {

    /**
     * 创建并异步启动自动化执行任务。
     */
    AutomationRunTaskVO start(AutomationRunStartRequest request);

    /**
     * 查询执行任务列表。
     */
    List<AutomationRunTaskVO> list(AutomationRunQueryRequest request);

    /**
     * 查询执行详情。
     */
    AutomationRunDetailVO detail(String runTaskId);

    /**
     * 取消执行任务。
     */
    Boolean cancel(AutomationRunCancelRequest request);
}