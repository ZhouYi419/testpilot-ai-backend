package com.zy.testpilotai.rageval.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.rageval.model.dto.RagEvalDatasetCreateRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalDatasetDeleteRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalDatasetQueryRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalDatasetUpdateRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalQuestionCreateRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalQuestionDeleteRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalQuestionQueryRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalQuestionUpdateRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalRunQueryRequest;
import com.zy.testpilotai.rageval.model.dto.RagEvalRunRequest;
import com.zy.testpilotai.rageval.model.vo.RagEvalDatasetVO;
import com.zy.testpilotai.rageval.model.vo.RagEvalQuestionVO;
import com.zy.testpilotai.rageval.model.vo.RagEvalRunDetailVO;
import com.zy.testpilotai.rageval.model.vo.RagEvalRunVO;
import com.zy.testpilotai.rageval.service.RagEvalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/rag-eval")
@RequiredArgsConstructor
public class RagEvalController {

    private final RagEvalService ragEvalService;

    /**
     * 创建 RAG 评测集。
     */
    @PostMapping("/dataset/create")
    public BaseResponse<RagEvalDatasetVO> createDataset(
            @RequestBody RagEvalDatasetCreateRequest request
    ) {
        return ResultUtils.success(ragEvalService.createDataset(request));
    }

    /**
     * 修改 RAG 评测集。
     */
    @PostMapping("/dataset/update")
    public BaseResponse<RagEvalDatasetVO> updateDataset(
            @RequestBody RagEvalDatasetUpdateRequest request
    ) {
        return ResultUtils.success(ragEvalService.updateDataset(request));
    }

    /**
     * 查询 RAG 评测集列表。
     */
    @PostMapping("/dataset/list")
    public BaseResponse<List<RagEvalDatasetVO>> listDatasets(
            @RequestBody RagEvalDatasetQueryRequest request
    ) {
        return ResultUtils.success(ragEvalService.listDatasets(request));
    }

    /**
     * 删除 RAG 评测集。
     */
    @PostMapping("/dataset/delete")
    public BaseResponse<Boolean> deleteDataset(
            @RequestBody RagEvalDatasetDeleteRequest request
    ) {
        return ResultUtils.success(ragEvalService.deleteDataset(request));
    }

    /**
     * 创建 RAG 评测问题。
     */
    @PostMapping("/question/create")
    public BaseResponse<RagEvalQuestionVO> createQuestion(
            @RequestBody RagEvalQuestionCreateRequest request
    ) {
        return ResultUtils.success(ragEvalService.createQuestion(request));
    }

    /**
     * 修改 RAG 评测问题。
     */
    @PostMapping("/question/update")
    public BaseResponse<RagEvalQuestionVO> updateQuestion(
            @RequestBody RagEvalQuestionUpdateRequest request
    ) {
        return ResultUtils.success(ragEvalService.updateQuestion(request));
    }

    /**
     * 查询 RAG 评测问题列表。
     */
    @PostMapping("/question/list")
    public BaseResponse<List<RagEvalQuestionVO>> listQuestions(
            @RequestBody RagEvalQuestionQueryRequest request
    ) {
        return ResultUtils.success(ragEvalService.listQuestions(request));
    }

    /**
     * 删除 RAG 评测问题。
     */
    @PostMapping("/question/delete")
    public BaseResponse<Boolean> deleteQuestion(
            @RequestBody RagEvalQuestionDeleteRequest request
    ) {
        return ResultUtils.success(ragEvalService.deleteQuestion(request));
    }

    /**
     * 运行 RAG 评测。
     */
    @PostMapping("/run")
    public BaseResponse<RagEvalRunDetailVO> run(
            @RequestBody RagEvalRunRequest request
    ) {
        return ResultUtils.success(ragEvalService.run(request));
    }

    /**
     * 查询 RAG 评测运行列表。
     */
    @PostMapping("/run/list")
    public BaseResponse<List<RagEvalRunVO>> listRuns(
            @RequestBody RagEvalRunQueryRequest request
    ) {
        return ResultUtils.success(ragEvalService.listRuns(request));
    }

    /**
     * 查询 RAG 评测运行详情。
     */
    @GetMapping("/run/{runId}")
    public BaseResponse<RagEvalRunDetailVO> runDetail(
            @PathVariable String runId
    ) {
        return ResultUtils.success(ragEvalService.runDetail(runId));
    }
}