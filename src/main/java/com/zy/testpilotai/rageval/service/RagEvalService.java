package com.zy.testpilotai.rageval.service;

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
import java.util.List;

public interface RagEvalService {

    RagEvalDatasetVO createDataset(RagEvalDatasetCreateRequest request);

    RagEvalDatasetVO updateDataset(RagEvalDatasetUpdateRequest request);

    List<RagEvalDatasetVO> listDatasets(RagEvalDatasetQueryRequest request);

    Boolean deleteDataset(RagEvalDatasetDeleteRequest request);

    RagEvalQuestionVO createQuestion(RagEvalQuestionCreateRequest request);

    RagEvalQuestionVO updateQuestion(RagEvalQuestionUpdateRequest request);

    List<RagEvalQuestionVO> listQuestions(RagEvalQuestionQueryRequest request);

    Boolean deleteQuestion(RagEvalQuestionDeleteRequest request);

    RagEvalRunDetailVO run(RagEvalRunRequest request);

    List<RagEvalRunVO> listRuns(RagEvalRunQueryRequest request);

    RagEvalRunDetailVO runDetail(String runId);
}