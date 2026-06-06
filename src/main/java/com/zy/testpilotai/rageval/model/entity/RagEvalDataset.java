package com.zy.testpilotai.rageval.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("rag_eval_dataset")
public class RagEvalDataset {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 评测集业务 ID。
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
     * 评测集名称。
     */
    private String datasetName;

    /**
     * 评测集描述。
     */
    private String description;

    /**
     * 状态：ACTIVE / DELETED。
     */
    private String status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}