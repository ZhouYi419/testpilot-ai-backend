package com.zy.testpilotai.testcase.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("test_case_set_item")
public class TestCaseSetItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用例集业务 ID。
     */
    private String caseSetId;

    /**
     * 测试用例数据库 ID。
     */
    private Long testCaseId;

    /**
     * 用例在用例集内的排序。
     */
    private Integer itemOrder;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}