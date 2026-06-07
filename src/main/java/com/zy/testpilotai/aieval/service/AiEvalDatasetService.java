package com.zy.testpilotai.aieval.service;

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
import java.util.List;

public interface AiEvalDatasetService {

    /**
     * 创建 AI 应用测试数据集。
     */
    AiEvalDatasetVO createDataset(AiEvalDatasetCreateRequest request);

    /**
     * 修改 AI 应用测试数据集。
     */
    AiEvalDatasetVO updateDataset(AiEvalDatasetUpdateRequest request);

    /**
     * 查询 AI 应用测试数据集列表。
     */
    List<AiEvalDatasetVO> listDatasets(AiEvalDatasetQueryRequest request);

    /**
     * 查询 AI 应用测试数据集详情。
     */
    AiEvalDatasetDetailVO detail(String datasetId);

    /**
     * 删除 AI 应用测试数据集。
     */
    Boolean deleteDataset(AiEvalDatasetDeleteRequest request);

    /**
     * 创建 AI 应用测试样本。
     */
    AiEvalCaseVO createCase(AiEvalCaseCreateRequest request);

    /**
     * 批量创建 AI 应用测试样本。
     */
    List<AiEvalCaseVO> batchCreateCases(AiEvalCaseBatchCreateRequest request);

    /**
     * 修改 AI 应用测试样本。
     */
    AiEvalCaseVO updateCase(AiEvalCaseUpdateRequest request);

    /**
     * 查询 AI 应用测试样本列表。
     */
    List<AiEvalCaseVO> listCases(AiEvalCaseQueryRequest request);

    /**
     * 删除 AI 应用测试样本。
     */
    Boolean deleteCase(AiEvalCaseDeleteRequest request);

    /**
     * 初始化内置 AI 应用测试样本。
     */
    List<AiEvalCaseVO> initPresetCases(AiEvalPresetInitRequest request);
}