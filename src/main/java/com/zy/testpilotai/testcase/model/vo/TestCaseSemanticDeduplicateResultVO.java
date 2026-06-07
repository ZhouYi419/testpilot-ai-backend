package com.zy.testpilotai.testcase.model.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试用例语义去重任务结果/聚合视图对象 VO
 */
@Data
public class TestCaseSemanticDeduplicateResultVO {

    /**
     * 去重任务全局唯一编号
     */
    private String deduplicateTaskId;

    /**
     * 执行对比的范围策略
     */
    private String compareScope;

    /**
     * 关联的用例生成任务ID
     */
    private String taskId;

    /**
     * 归属的项目ID
     */
    private Long projectId;

    /**
     * 归属的版本号
     */
    private String versionNo;

    /**
     * 归属的模块编码
     */
    private String moduleCode;

    /**
     * 向量检索的相似度阈值
     */
    private Double threshold;

    /**
     * 向量召回数量上限
     */
    private Integer topK;

    /**
     * 是否强制重建 Embedding 向量
     */
    private Integer rebuildEmbedding;

    /**
     * 去重任务的当前状态
     */
    private String status;

    /**
     * 本次参与查重排查的源用例总数
     */
    private Integer sourceCaseCount;

    /**
     * 向量库中被检索的候选底库用例总数
     */
    private Integer candidateCaseCount;

    /**
     * 找到的疑似重复对数量
     */
    private Integer duplicatePairCount;

    /**
     * 最终被确认标记为重复的数量
     */
    private Integer markedDuplicateCount;

    /**
     * 去重分析报告总结
     */
    private String summary;

    /**
     * 任务执行异常/报错信息
     */
    private String errorMessage;

    /**
     * 任务创建时间
     */
    private LocalDateTime createTime;

    /**
     * 任务最后更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 具体的相似用例对明细列表
     */
    private List<TestCaseSemanticDuplicatePairVO> duplicatePairs = new ArrayList<>();
}