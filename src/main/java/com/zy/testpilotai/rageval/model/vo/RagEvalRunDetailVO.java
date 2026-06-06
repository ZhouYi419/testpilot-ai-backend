package com.zy.testpilotai.rageval.model.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class RagEvalRunDetailVO {

    /**
     * 评测运行任务。
     */
    private RagEvalRunVO run;

    /**
     * 每道问题的评测结果。
     */
    private List<RagEvalResultVO> results = new ArrayList<>();
}