package com.zy.testpilotai.llm.structured.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiGeneratedTestCaseOutputDTO {

    /**
     * 测试用例列表。
     */
    private List<AiGeneratedTestCaseItemDTO> testCases = new ArrayList<>();
}