package com.zy.testpilotai.llm.structured.dto;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class AiAutomationScriptOutputDTO {

    /**
     * 文件列表。
     */
    private List<AiAutomationScriptFileDTO> files = new ArrayList<>();
}