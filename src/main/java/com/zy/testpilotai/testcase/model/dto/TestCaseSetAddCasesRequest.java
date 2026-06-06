package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;
import java.util.List;

@Data
public class TestCaseSetAddCasesRequest {

    /**
     * 用例集业务 ID。
     */
    private String caseSetId;

    /**
     * 测试用例 ID 列表。
     */
    private List<Long> testCaseIds;
}