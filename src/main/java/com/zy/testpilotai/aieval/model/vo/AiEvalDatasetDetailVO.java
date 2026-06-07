package com.zy.testpilotai.aieval.model.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiEvalDatasetDetailVO {

    /**
     * 数据集信息。
     */
    private AiEvalDatasetVO dataset;

    /**
     * 样本列表。
     */
    private List<AiEvalCaseVO> cases = new ArrayList<>();
}