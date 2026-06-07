package com.zy.testpilotai.llm.structured.dto;

import lombok.Data;

@Data
public class AiAutomationScriptFileDTO {

    /**
     * 文件路径，例如 tests/test_member_recharge.py。
     */
    private String filePath;

    /**
     * 文件类型：
     * PYTHON / TEXT / CONFIG / MARKDOWN。
     */
    private String fileType;

    /**
     * 文件说明。
     */
    private String description;

    /**
     * 文件内容。
     */
    private String content;
}