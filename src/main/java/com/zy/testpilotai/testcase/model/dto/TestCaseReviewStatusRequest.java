package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class TestCaseReviewStatusRequest {

    /**
     * 测试用例 ID 列表。
     */
    private List<Long> ids;

    /**
     * 审核人。
     */
    private String reviewer;

    /**
     * 人工备注。
     */
    private String manualComment;
}