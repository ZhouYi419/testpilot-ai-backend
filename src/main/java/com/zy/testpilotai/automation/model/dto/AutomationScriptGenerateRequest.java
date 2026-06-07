package com.zy.testpilotai.automation.model.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AutomationScriptGenerateRequest {

    /**
     * 来源类型：
     * CASE_SET：从用例集生成
     * TASK：从测试用例生成任务生成
     * IDS：从指定测试用例 ID 生成
     */
    private String sourceType;

    /**
     * 用例集 ID。
     */
    private String caseSetId;

    /**
     * 测试用例生成任务 ID。
     */
    private String taskId;

    /**
     * 指定测试用例 ID。
     */
    private List<Long> testCaseIds;

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
     * 基础 URL。
     */
    private String baseUrl;

    /**
     * 鉴权类型：
     * NONE / BEARER_TOKEN / CUSTOM_HEADER。
     */
    private String authType;

    /**
     * 鉴权 Header 名称。
     */
    private String authHeaderName;

    /**
     * Token 占位符。
     */
    private String tokenPlaceholder;

    /**
     * 公共 Header。
     */
    private Map<String, String> commonHeaders;

    /**
     * 生成模式：
     * LLM / TEMPLATE。
     */
    private String generateMode;

    /**
     * 是否只使用已采纳用例。
     * 默认 false。
     */
    private Boolean acceptedOnly;
}