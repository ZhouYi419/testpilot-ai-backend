package com.zy.testpilotai.aieval.model.vo;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiEvalRunDetailVO {

    /**
     * 运行任务。
     */
    private AiEvalRunVO run;

    /**
     * 单样本执行结果。
     */
    private List<AiEvalResultVO> results = new ArrayList<>();
}