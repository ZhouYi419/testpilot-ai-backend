package com.zy.testpilotai.testcase.service;

import com.zy.testpilotai.testcase.model.dto.TestCaseGenerateRequest;
import com.zy.testpilotai.testcase.model.vo.TestCaseGenerateResponseVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseVO;
import java.util.List;

public interface TestCaseGenerationService {

    TestCaseGenerateResponseVO generate(TestCaseGenerateRequest request);

    List<TestCaseVO> listByProject(Long projectId, String versionName, String moduleName);
}