package com.zy.testpilotai.aiapp.service;

import com.zy.testpilotai.aiapp.model.dto.AiAppTestCaseListRequest;
import com.zy.testpilotai.aiapp.model.dto.AiAppTestGenerateRequest;
import com.zy.testpilotai.aiapp.model.vo.AiAppTestCaseVO;
import com.zy.testpilotai.aiapp.model.vo.AiAppTestGenerateResultVO;
import com.zy.testpilotai.aiapp.model.vo.AiAppTestTaskVO;
import java.util.List;

public interface AiAppTestService {

    /**
     * 生成 AI 应用专项测试用例。
     */
    AiAppTestGenerateResultVO generate(AiAppTestGenerateRequest request);

    /**
     * 查询 AI 应用测试用例列表。
     */
    List<AiAppTestCaseVO> listCases(AiAppTestCaseListRequest request);

    /**
     * 查询 AI 应用测试生成任务。
     */
    AiAppTestTaskVO getTask(String taskId);
}