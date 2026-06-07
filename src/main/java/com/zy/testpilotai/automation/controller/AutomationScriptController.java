package com.zy.testpilotai.automation.controller;

import com.zy.testpilotai.automation.model.dto.AutomationScriptGenerateRequest;
import com.zy.testpilotai.automation.model.dto.AutomationScriptQueryRequest;
import com.zy.testpilotai.automation.model.vo.AutomationScriptDetailVO;
import com.zy.testpilotai.automation.model.vo.AutomationScriptTaskVO;
import com.zy.testpilotai.automation.service.AutomationScriptService;
import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/automation/script")
@RequiredArgsConstructor
public class AutomationScriptController {

    private final AutomationScriptService automationScriptService;

    /**
     * 生成接口自动化脚本。
     */
    @PostMapping("/generate")
    public BaseResponse<AutomationScriptDetailVO> generate(
            @RequestBody AutomationScriptGenerateRequest request
    ) {
        return ResultUtils.success(automationScriptService.generate(request));
    }

    /**
     * 查询脚本生成任务列表。
     */
    @PostMapping("/list")
    public BaseResponse<List<AutomationScriptTaskVO>> list(
            @RequestBody AutomationScriptQueryRequest request
    ) {
        return ResultUtils.success(automationScriptService.list(request));
    }

    /**
     * 查询脚本生成任务详情。
     */
    @GetMapping("/detail/{scriptTaskId}")
    public BaseResponse<AutomationScriptDetailVO> detail(
            @PathVariable String scriptTaskId
    ) {
        return ResultUtils.success(automationScriptService.detail(scriptTaskId));
    }

    /**
     * 下载脚本 zip 包。
     */
    @GetMapping("/download/{scriptTaskId}")
    public void download(
            @PathVariable String scriptTaskId,
            HttpServletResponse response
    ) {
        automationScriptService.download(scriptTaskId, response);
    }
}