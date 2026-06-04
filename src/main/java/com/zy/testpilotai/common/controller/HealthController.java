package com.zy.testpilotai.common.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public BaseResponse<String> health() {
        return ResultUtils.success("TestPilot AI backend is running");
    }
}