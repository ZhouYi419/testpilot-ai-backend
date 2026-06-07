package com.zy.testpilotai.aieval.controller;

import com.zy.testpilotai.aieval.model.dto.AiEvalAppConfigCreateRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalAppConfigDeleteRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalAppConfigQueryRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalAppConfigUpdateRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalRunQueryRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalRunRequest;
import com.zy.testpilotai.aieval.model.vo.AiEvalAppConfigVO;
import com.zy.testpilotai.aieval.model.vo.AiEvalRunDetailVO;
import com.zy.testpilotai.aieval.model.vo.AiEvalRunVO;
import com.zy.testpilotai.aieval.service.AiEvalRunnerService;
import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai-eval/runner")
@RequiredArgsConstructor
public class AiEvalRunnerController {

    private final AiEvalRunnerService aiEvalRunnerService;

    /**
     * 创建待测 AI 应用配置。
     */
    @PostMapping("/app-config/create")
    public BaseResponse<AiEvalAppConfigVO> createAppConfig(
            @RequestBody AiEvalAppConfigCreateRequest request
    ) {
        return ResultUtils.success(aiEvalRunnerService.createAppConfig(request));
    }

    /**
     * 修改待测 AI 应用配置。
     */
    @PostMapping("/app-config/update")
    public BaseResponse<AiEvalAppConfigVO> updateAppConfig(
            @RequestBody AiEvalAppConfigUpdateRequest request
    ) {
        return ResultUtils.success(aiEvalRunnerService.updateAppConfig(request));
    }

    /**
     * 查询待测 AI 应用配置列表。
     */
    @PostMapping("/app-config/list")
    public BaseResponse<List<AiEvalAppConfigVO>> listAppConfigs(
            @RequestBody AiEvalAppConfigQueryRequest request
    ) {
        return ResultUtils.success(aiEvalRunnerService.listAppConfigs(request));
    }

    /**
     * 删除待测 AI 应用配置。
     */
    @PostMapping("/app-config/delete")
    public BaseResponse<Boolean> deleteAppConfig(
            @RequestBody AiEvalAppConfigDeleteRequest request
    ) {
        return ResultUtils.success(aiEvalRunnerService.deleteAppConfig(request));
    }

    /**
     * 运行 AI 应用测试。
     */
    @PostMapping("/run")
    public BaseResponse<AiEvalRunDetailVO> run(
            @RequestBody AiEvalRunRequest request
    ) {
        return ResultUtils.success(aiEvalRunnerService.run(request));
    }

    /**
     * 查询 AI 应用测试运行列表。
     */
    @PostMapping("/run/list")
    public BaseResponse<List<AiEvalRunVO>> listRuns(
            @RequestBody AiEvalRunQueryRequest request
    ) {
        return ResultUtils.success(aiEvalRunnerService.listRuns(request));
    }

    /**
     * 查询 AI 应用测试运行详情。
     */
    @GetMapping("/run/detail/{runId}")
    public BaseResponse<AiEvalRunDetailVO> detail(
            @PathVariable String runId
    ) {
        return ResultUtils.success(aiEvalRunnerService.detail(runId));
    }
}