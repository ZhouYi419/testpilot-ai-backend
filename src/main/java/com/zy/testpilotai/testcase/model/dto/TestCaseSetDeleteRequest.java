package com.zy.testpilotai.testcase.model.dto;

import lombok.Data;

@Data
public class TestCaseSetDeleteRequest {

    /**
     * 用例集业务 ID。
     */
    private String caseSetId;
}