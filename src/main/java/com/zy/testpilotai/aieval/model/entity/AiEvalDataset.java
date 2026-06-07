package com.zy.testpilotai.aieval.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_eval_dataset")
public class AiEvalDataset {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 数据集业务 ID。
     */
    private String datasetId;

    /**
     * 项目 ID。
     */
    private Long projectId;

    /**
     * 版本号。
     */
    private String versionNo;

    /**
     * 模块编码。
     */
    private String moduleCode;

    /**
     * 数据集名称。
     */
    private String datasetName;

    /**
     * 数据集类型：
     * RAG / LLM / AGENT / PROMPT / SAFETY / MIXED。
     */
    private String datasetType;

    /**
     * 描述。
     */
    private String description;

    /**
     * 样本数量。
     */
    private Integer caseCount;

    /**
     * 状态：
     * ACTIVE / DELETED。
     */
    private String status;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}