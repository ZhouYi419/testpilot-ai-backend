package com.zy.testpilotai.llm.structured.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiAppTestCaseOutputDTO {

    /**
     * AI 应用测试用例列表。
     */
    private List<AiAppTestCaseItemDTO> testCases = new ArrayList<>();
}