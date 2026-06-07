package com.zy.testpilotai.aieval.service;

import com.zy.testpilotai.aieval.model.dto.AiEvalAppConfigCreateRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalAppConfigDeleteRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalAppConfigQueryRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalAppConfigUpdateRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalRunQueryRequest;
import com.zy.testpilotai.aieval.model.dto.AiEvalRunRequest;
import com.zy.testpilotai.aieval.model.vo.AiEvalAppConfigVO;
import com.zy.testpilotai.aieval.model.vo.AiEvalRunDetailVO;
import com.zy.testpilotai.aieval.model.vo.AiEvalRunVO;
import java.util.List;

public interface AiEvalRunnerService {

    /**
     * 创建待测 AI 应用配置。
     */
    AiEvalAppConfigVO createAppConfig(AiEvalAppConfigCreateRequest request);

    /**
     * 修改待测 AI 应用配置。
     */
    AiEvalAppConfigVO updateAppConfig(AiEvalAppConfigUpdateRequest request);

    /**
     * 查询待测 AI 应用配置。
     */
    List<AiEvalAppConfigVO> listAppConfigs(AiEvalAppConfigQueryRequest request);

    /**
     * 删除待测 AI 应用配置。
     */
    Boolean deleteAppConfig(AiEvalAppConfigDeleteRequest request);

    /**
     * 运行 AI 应用测试。
     */
    AiEvalRunDetailVO run(AiEvalRunRequest request);

    /**
     * 查询运行任务列表。
     */
    List<AiEvalRunVO> listRuns(AiEvalRunQueryRequest request);

    /**
     * 查询运行详情。
     */
    AiEvalRunDetailVO detail(String runId);
}