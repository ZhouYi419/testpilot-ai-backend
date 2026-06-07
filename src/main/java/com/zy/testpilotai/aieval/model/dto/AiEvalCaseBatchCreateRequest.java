package com.zy.testpilotai.aieval.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiEvalCaseBatchCreateRequest {

    /**
     * 样本列表。
     */
    private List<AiEvalCaseCreateRequest> cases;
}