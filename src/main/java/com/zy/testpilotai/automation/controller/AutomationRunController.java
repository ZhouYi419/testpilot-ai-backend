package com.zy.testpilotai.automation.controller;

import com.zy.testpilotai.automation.model.dto.AutomationRunCancelRequest;
import com.zy.testpilotai.automation.model.dto.AutomationRunQueryRequest;
import com.zy.testpilotai.automation.model.dto.AutomationRunStartRequest;
import com.zy.testpilotai.automation.model.vo.AutomationRunDetailVO;
import com.zy.testpilotai.automation.model.vo.AutomationRunTaskVO;
import com.zy.testpilotai.automation.service.AutomationRunService;
import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/automation/run")
@RequiredArgsConstructor
public class AutomationRunController {

    private final AutomationRunService automationRunService;

    /**
     * 启动自动化执行任务。
     */
    @PostMapping("/start")
    public BaseResponse<AutomationRunTaskVO> start(
            @RequestBody AutomationRunStartRequest request
    ) {
        return ResultUtils.success(automationRunService.start(request));
    }

    /**
     * 查询自动化执行任务列表。
     */
    @PostMapping("/list")
    public BaseResponse<List<AutomationRunTaskVO>> list(
            @RequestBody AutomationRunQueryRequest request
    ) {
        return ResultUtils.success(automationRunService.list(request));
    }

    /**
     * 查询自动化执行任务详情。
     */
    @GetMapping("/detail/{runTaskId}")
    public BaseResponse<AutomationRunDetailVO> detail(
            @PathVariable String runTaskId
    ) {
        return ResultUtils.success(automationRunService.detail(runTaskId));
    }

    /**
     * 取消自动化执行任务。
     */
    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancel(
            @RequestBody AutomationRunCancelRequest request
    ) {
        return ResultUtils.success(automationRunService.cancel(request));
    }
}