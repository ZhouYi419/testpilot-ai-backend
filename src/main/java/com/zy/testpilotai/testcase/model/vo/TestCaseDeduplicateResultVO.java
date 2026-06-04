package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.util.List;

@Data
public class TestCaseDeduplicateResultVO {

    /**
     * 本次参与去重的用例数量
     */
    private Integer totalCaseCount;

    /**
     * 被标记为重复的用例数量
     */
    private Integer duplicateCaseCount;

    /**
     * 去重阈值
     */
    private Double threshold;

    /**
     * 重复用例明细
     */
    private List<DuplicateCaseVO> duplicateCases;
}