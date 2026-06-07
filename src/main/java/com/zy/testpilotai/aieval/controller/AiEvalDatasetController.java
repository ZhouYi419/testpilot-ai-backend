package com.zy.testpilotai.aieval.controller;

import com.zy.testpilotai.aieval.model.dto.AiEvalCaseBatchCreateRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalCaseCreateRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalCaseDeleteRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalCaseQueryRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalCaseUpdateRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalDatasetCreateRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalDatasetDeleteRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalDatasetQueryRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalDatasetUpdateRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalPresetInitRequest;
import com.zy.testpilotai.aieval.model.vo.AiEvalCaseVO;
import com.zy.testpilotai.aieval.model.vo.AiEvalDatasetDetailVO;
import com.zy.testpilotai.aieval.model.vo.AiEvalDatasetVO;
import com.zy.testpilotai.aieval.service.AiEvalDatasetService;
import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/ai-eval/dataset")
@RequiredArgsConstructor
public class AiEvalDatasetController {

    private final AiEvalDatasetService aiEvalDatasetService;

    /**
     * 创建 AI 应用测试数据集。
     */
    @PostMapping("/create")
    public BaseResponse<AiEvalDatasetVO> createDataset(
            @RequestBody AiEvalDatasetCreateRequest request
    ) {
        return ResultUtils.success(aiEvalDatasetService.createDataset(request));
    }

    /**
     * 修改 AI 应用测试数据集。
     */
    @PostMapping("/update")
    public BaseResponse<AiEvalDatasetVO> updateDataset(
            @RequestBody AiEvalDatasetUpdateRequest request
    ) {
        return ResultUtils.success(aiEvalDatasetService.updateDataset(request));
    }

    /**
     * 查询 AI 应用测试数据集列表。
     */
    @PostMapping("/list")
    public BaseResponse<List<AiEvalDatasetVO>> listDatasets(
            @RequestBody AiEvalDatasetQueryRequest request
    ) {
        return ResultUtils.success(aiEvalDatasetService.listDatasets(request));
    }

    /**
     * 查询 AI 应用测试数据集详情。
     */
    @GetMapping("/detail/{datasetId}")
    public BaseResponse<AiEvalDatasetDetailVO> detail(
            @PathVariable String datasetId
    ) {
        return ResultUtils.success(aiEvalDatasetService.detail(datasetId));
    }

    /**
     * 删除 AI 应用测试数据集。
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteDataset(
            @RequestBody AiEvalDatasetDeleteRequest request
    ) {
        return ResultUtils.success(aiEvalDatasetService.deleteDataset(request));
    }

    /**
     * 创建 AI 应用测试样本。
     */
    @PostMapping("/case/create")
    public BaseResponse<AiEvalCaseVO> createCase(
            @RequestBody AiEvalCaseCreateRequest request
    ) {
        return ResultUtils.success(aiEvalDatasetService.createCase(request));
    }

    /**
     * 批量创建 AI 应用测试样本。
     */
    @PostMapping("/case/batch-create")
    public BaseResponse<List<AiEvalCaseVO>> batchCreateCases(
            @RequestBody AiEvalCaseBatchCreateRequest request
    ) {
        return ResultUtils.success(aiEvalDatasetService.batchCreateCases(request));
    }

    /**
     * 修改 AI 应用测试样本。
     */
    @PostMapping("/case/update")
    public BaseResponse<AiEvalCaseVO> updateCase(
            @RequestBody AiEvalCaseUpdateRequest request
    ) {
        return ResultUtils.success(aiEvalDatasetService.updateCase(request));
    }

    /**
     * 查询 AI 应用测试样本列表。
     */
    @PostMapping("/case/list")
    public BaseResponse<List<AiEvalCaseVO>> listCases(
            @RequestBody AiEvalCaseQueryRequest request
    ) {
        return ResultUtils.success(aiEvalDatasetService.listCases(request));
    }

    /**
     * 删除 AI 应用测试样本。
     */
    @PostMapping("/case/delete")
    public BaseResponse<Boolean> deleteCase(
            @RequestBody AiEvalCaseDeleteRequest request
    ) {
        return ResultUtils.success(aiEvalDatasetService.deleteCase(request));
    }

    /**
     * 初始化内置 AI 应用测试样本。
     */
    @PostMapping("/preset/init")
    public BaseResponse<List<AiEvalCaseVO>> initPresetCases(
            @RequestBody AiEvalPresetInitRequest request
    ) {
        return ResultUtils.success(aiEvalDatasetService.initPresetCases(request));
    }
}