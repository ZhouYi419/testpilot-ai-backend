package com.zy.testpilotai.automation.service;

import com.zy.testpilotai.automation.model.dto.AutomationScriptGenerateRequest;
import com.zy.testpilotai.automation.model.dto.AutomationScriptQueryRequest;
import com.zy.testpilotai.automation.model.vo.AutomationScriptDetailVO;
import com.zy.testpilotai.automation.model.vo.AutomationScriptTaskVO;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;

public interface AutomationScriptService {

    /**
     * 生成自动化脚本。
     */
    AutomationScriptDetailVO generate(AutomationScriptGenerateRequest request);

    /**
     * 查询脚本任务列表。
     */
    List<AutomationScriptTaskVO> list(AutomationScriptQueryRequest request);

    /**
     * 查询脚本任务详情。
     */
    AutomationScriptDetailVO detail(String scriptTaskId);

    /**
     * 下载脚本 zip 包。
     */
    void download(String scriptTaskId, HttpServletResponse response);
}