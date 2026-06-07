package com.zy.testpilotai.automation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.automation.mapper.AutomationScriptFileMapper;
import com.zy.testpilotai.automation.mapper.AutomationScriptTaskMapper;
import com.zy.testpilotai.automation.model.dto.AutomationScriptGenerateRequest;
import com.zy.testpilotai.automation.model.dto.AutomationScriptQueryRequest;
import com.zy.testpilotai.automation.model.entity.AutomationScriptFile;
import com.zy.testpilotai.automation.model.entity.AutomationScriptTask;
import com.zy.testpilotai.automation.model.vo.AutomationScriptDetailVO;
import com.zy.testpilotai.automation.model.vo.AutomationScriptFileVO;
import com.zy.testpilotai.automation.model.vo.AutomationScriptTaskVO;
import com.zy.testpilotai.automation.service.AutomationScriptService;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.llm.chat.LlmClient;
import com.zy.testpilotai.llm.structured.AiAutomationScriptOutputParser;
import com.zy.testpilotai.llm.structured.dto.AiAutomationScriptFileDTO;
import com.zy.testpilotai.llm.structured.dto.AiAutomationScriptOutputDTO;
import com.zy.testpilotai.testcase.mapper.TestCaseMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSetItemMapper;
import com.zy.testpilotai.testcase.mapper.TestCaseSetMapper;
import com.zy.testpilotai.testcase.model.entity.TestCase;
import com.zy.testpilotai.testcase.model.entity.TestCaseSet;
import com.zy.testpilotai.testcase.model.entity.TestCaseSetItem;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class AutomationScriptServiceImpl implements AutomationScriptService {

    private final AutomationScriptTaskMapper automationScriptTaskMapper;

    private final AutomationScriptFileMapper automationScriptFileMapper;

    private final TestCaseMapper testCaseMapper;

    private final TestCaseSetMapper testCaseSetMapper;

    private final TestCaseSetItemMapper testCaseSetItemMapper;

    private final LlmClient llmClient;

    private final AiAutomationScriptOutputParser aiAutomationScriptOutputParser;

    private final ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AutomationScriptDetailVO generate(AutomationScriptGenerateRequest request) {
        validateGenerateRequest(request);

        String sourceType = normalizeSourceType(request.getSourceType());
        String generateMode = normalizeGenerateMode(request.getGenerateMode());

        List<TestCase> testCases = loadTestCases(request, sourceType);

        if (testCases.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "没有找到可生成自动化脚本的测试用例"
            );
        }

        String scriptTaskId = "as_" + UUID.randomUUID().toString().replace("-", "");

        AutomationScriptTask task = createTask(
                scriptTaskId,
                request,
                sourceType,
                generateMode,
                testCases
        );

        try {
            List<AiAutomationScriptFileDTO> files;

            if ("TEMPLATE".equals(generateMode)) {
                files = buildTemplateFiles(task, testCases);
            } else {
                String systemPrompt = buildSystemPrompt();
                String userPrompt = buildUserPrompt(task, testCases);

                String rawOutput = llmClient.chat(
                        systemPrompt,
                        userPrompt,
                        "AUTOMATION_SCRIPT_GENERATE",
                        scriptTaskId
                );

                task.setRawModelOutput(rawOutput);
                task.setUpdateTime(LocalDateTime.now());
                automationScriptTaskMapper.updateById(task);

                AiAutomationScriptOutputDTO output =
                        aiAutomationScriptOutputParser.parse(rawOutput);

                files = output.getFiles();

                files = ensureRequiredFiles(files, task, testCases);
            }

            int fileCount = saveFiles(scriptTaskId, files);

            Map<String, Object> summary = new LinkedHashMap<>();
            summary.put("scriptTaskId", scriptTaskId);
            summary.put("sourceType", sourceType);
            summary.put("generateMode", generateMode);
            summary.put("caseCount", testCases.size());
            summary.put("fileCount", fileCount);
            summary.put("framework", "PYTEST_REQUESTS");
            summary.put("downloadApi", "/api/automation/script/download/" + scriptTaskId);

            task.setStatus("SUCCESS");
            task.setFileCount(fileCount);
            task.setSummary(toJson(summary));
            task.setUpdateTime(LocalDateTime.now());

            automationScriptTaskMapper.updateById(task);

            return detail(scriptTaskId);
        } catch (BusinessException e) {
            markFailed(task, e.getMessage());
            throw e;
        } catch (Exception e) {
            markFailed(task, e.getMessage());
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "自动化脚本生成失败：" + e.getMessage()
            );
        }
    }

    @Override
    public List<AutomationScriptTaskVO> list(AutomationScriptQueryRequest request) {
        LambdaQueryWrapper<AutomationScriptTask> wrapper =
                new LambdaQueryWrapper<AutomationScriptTask>()
                        .orderByDesc(AutomationScriptTask::getCreateTime)
                        .orderByDesc(AutomationScriptTask::getId);

        if (request != null && StringUtils.hasText(request.getSourceType())) {
            wrapper.eq(
                    AutomationScriptTask::getSourceType,
                    normalizeSourceType(request.getSourceType())
            );
        }

        if (request != null && StringUtils.hasText(request.getCaseSetId())) {
            wrapper.eq(AutomationScriptTask::getCaseSetId, request.getCaseSetId());
        }

        if (request != null && StringUtils.hasText(request.getTaskId())) {
            wrapper.eq(AutomationScriptTask::getTestcaseTaskId, request.getTaskId());
        }

        if (request != null && request.getProjectId() != null) {
            wrapper.eq(AutomationScriptTask::getProjectId, request.getProjectId());
        }

        if (request != null && StringUtils.hasText(request.getVersionNo())) {
            wrapper.eq(AutomationScriptTask::getVersionNo, request.getVersionNo());
        }

        if (request != null && StringUtils.hasText(request.getModuleCode())) {
            wrapper.eq(AutomationScriptTask::getModuleCode, request.getModuleCode());
        }

        if (request != null && StringUtils.hasText(request.getStatus())) {
            wrapper.eq(AutomationScriptTask::getStatus, request.getStatus());
        }

        return automationScriptTaskMapper.selectList(wrapper)
                .stream()
                .map(this::toTaskVO)
                .toList();
    }

    @Override
    public AutomationScriptDetailVO detail(String scriptTaskId) {
        if (!StringUtils.hasText(scriptTaskId)) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "脚本任务 ID 不能为空"
            );
        }

        AutomationScriptTask task = getTask(scriptTaskId);

        List<AutomationScriptFileVO> files =
                automationScriptFileMapper.selectList(
                                new LambdaQueryWrapper<AutomationScriptFile>()
                                        .eq(AutomationScriptFile::getScriptTaskId, scriptTaskId)
                                        .orderByAsc(AutomationScriptFile::getId)
                        )
                        .stream()
                        .map(this::toFileVO)
                        .toList();

        AutomationScriptDetailVO detailVO = new AutomationScriptDetailVO();
        detailVO.setTask(toTaskVO(task));
        detailVO.setFiles(files);

        return detailVO;
    }

    @Override
    public void download(String scriptTaskId, HttpServletResponse response) {
        if (!StringUtils.hasText(scriptTaskId)) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "脚本任务 ID 不能为空"
            );
        }

        AutomationScriptTask task = getTask(scriptTaskId);

        if (!"SUCCESS".equals(task.getStatus())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "脚本任务未成功，无法下载"
            );
        }

        List<AutomationScriptFile> files =
                automationScriptFileMapper.selectList(
                        new LambdaQueryWrapper<AutomationScriptFile>()
                                .eq(AutomationScriptFile::getScriptTaskId, scriptTaskId)
                                .orderByAsc(AutomationScriptFile::getId)
                );

        if (files.isEmpty()) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "脚本文件不存在"
            );
        }

        try {
            String fileName = "api_automation_" + scriptTaskId + ".zip";
            String encodedFileName = URLEncoder.encode(
                    fileName,
                    StandardCharsets.UTF_8
            ).replaceAll("\\+", "%20");

            response.setContentType("application/zip");
            response.setCharacterEncoding("UTF-8");
            response.setHeader(
                    "Content-Disposition",
                    "attachment; filename*=UTF-8''" + encodedFileName
            );

            try (ZipOutputStream zipOutputStream =
                         new ZipOutputStream(response.getOutputStream(), StandardCharsets.UTF_8)) {

                for (AutomationScriptFile file : files) {
                    ZipEntry entry = new ZipEntry(file.getFilePath());
                    zipOutputStream.putNextEntry(entry);
                    zipOutputStream.write(file.getFileContent().getBytes(StandardCharsets.UTF_8));
                    zipOutputStream.closeEntry();
                }
            }
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "下载脚本 zip 失败：" + e.getMessage()
            );
        }
    }

    private void validateGenerateRequest(AutomationScriptGenerateRequest request) {
        if (request == null) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "生成请求不能为空"
            );
        }

        String sourceType = normalizeSourceType(request.getSourceType());

        if ("CASE_SET".equals(sourceType)
                && !StringUtils.hasText(request.getCaseSetId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "从用例集生成脚本时 caseSetId 不能为空"
            );
        }

        if ("TASK".equals(sourceType)
                && !StringUtils.hasText(request.getTaskId())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "从任务生成脚本时 taskId 不能为空"
            );
        }

        if ("IDS".equals(sourceType)
                && CollectionUtils.isEmpty(request.getTestCaseIds())) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "从指定用例生成脚本时 testCaseIds 不能为空"
            );
        }
    }

    private List<TestCase> loadTestCases(
            AutomationScriptGenerateRequest request,
            String sourceType
    ) {
        if ("CASE_SET".equals(sourceType)) {
            return loadCasesByCaseSet(request.getCaseSetId());
        }

        if ("TASK".equals(sourceType)) {
            return loadCasesByTask(request);
        }

        return loadCasesByIds(request.getTestCaseIds());
    }

    private List<TestCase> loadCasesByCaseSet(String caseSetId) {
        TestCaseSet caseSet = testCaseSetMapper.selectOne(
                new LambdaQueryWrapper<TestCaseSet>()
                        .eq(TestCaseSet::getCaseSetId, caseSetId)
                        .last("LIMIT 1")
        );

        if (caseSet == null || "DELETED".equals(caseSet.getStatus())) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "用例集不存在或已删除"
            );
        }

        List<TestCaseSetItem> items =
                testCaseSetItemMapper.selectList(
                        new LambdaQueryWrapper<TestCaseSetItem>()
                                .eq(TestCaseSetItem::getCaseSetId, caseSetId)
                                .orderByAsc(TestCaseSetItem::getItemOrder)
                                .orderByAsc(TestCaseSetItem::getId)
                );

        List<TestCase> result = new ArrayList<>();

        for (TestCaseSetItem item : items) {
            TestCase testCase = testCaseMapper.selectById(item.getTestCaseId());

            if (testCase == null) {
                continue;
            }

            if ("DELETED".equals(testCase.getReviewStatus())) {
                continue;
            }

            result.add(testCase);
        }

        return result;
    }

    private List<TestCase> loadCasesByTask(AutomationScriptGenerateRequest request) {
        LambdaQueryWrapper<TestCase> wrapper =
                new LambdaQueryWrapper<TestCase>()
                        .eq(TestCase::getTaskId, request.getTaskId())
                        .orderByAsc(TestCase::getId);

        appendCommonCaseFilters(wrapper, request);

        return testCaseMapper.selectList(wrapper);
    }

    private List<TestCase> loadCasesByIds(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }

        return testCaseMapper.selectList(
                new LambdaQueryWrapper<TestCase>()
                        .in(TestCase::getId, ids)
                        .and(w -> w
                                .ne(TestCase::getReviewStatus, "DELETED")
                                .or()
                                .isNull(TestCase::getReviewStatus)
                        )
                        .orderByAsc(TestCase::getId)
        );
    }

    private void appendCommonCaseFilters(
            LambdaQueryWrapper<TestCase> wrapper,
            AutomationScriptGenerateRequest request
    ) {
        if (request.getProjectId() != null) {
            wrapper.eq(TestCase::getProjectId, request.getProjectId());
        }

        if (StringUtils.hasText(request.getVersionNo())) {
            wrapper.eq(TestCase::getVersionNo, request.getVersionNo());
        }

        if (StringUtils.hasText(request.getModuleCode())) {
            wrapper.eq(TestCase::getModuleCode, request.getModuleCode());
        }

        if (Boolean.TRUE.equals(request.getAcceptedOnly())) {
            wrapper.eq(TestCase::getReviewStatus, "ACCEPTED");
        } else {
            wrapper.and(w -> w
                    .ne(TestCase::getReviewStatus, "DELETED")
                    .or()
                    .isNull(TestCase::getReviewStatus)
            );
        }
    }

    private AutomationScriptTask createTask(
            String scriptTaskId,
            AutomationScriptGenerateRequest request,
            String sourceType,
            String generateMode,
            List<TestCase> testCases
    ) {
        AutomationScriptTask task = new AutomationScriptTask();

        task.setScriptTaskId(scriptTaskId);
        task.setSourceType(sourceType);
        task.setCaseSetId(request.getCaseSetId());
        task.setTestcaseTaskId(request.getTaskId());

        Long projectId = request.getProjectId();
        String versionNo = request.getVersionNo();
        String moduleCode = request.getModuleCode();

        if (!testCases.isEmpty()) {
            TestCase firstCase = testCases.get(0);

            if (projectId == null) {
                projectId = firstCase.getProjectId();
            }

            if (!StringUtils.hasText(versionNo)) {
                versionNo = firstCase.getVersionNo();
            }

            if (!StringUtils.hasText(moduleCode)) {
                moduleCode = firstCase.getModuleCode();
            }
        }

        task.setProjectId(projectId);
        task.setVersionNo(versionNo);
        task.setModuleCode(moduleCode);
        task.setScriptFramework("PYTEST_REQUESTS");
        task.setGenerateMode(generateMode);
        task.setBaseUrl(defaultText(request.getBaseUrl(), "http://localhost:8080"));
        task.setAuthType(normalizeAuthType(request.getAuthType()));
        task.setAuthHeaderName(request.getAuthHeaderName());
        task.setTokenPlaceholder(defaultText(request.getTokenPlaceholder(), "${API_TOKEN}"));
        task.setCommonHeaders(toJson(request.getCommonHeaders() == null ? Map.of() : request.getCommonHeaders()));
        task.setSelectedCaseIds(toJson(testCases.stream().map(TestCase::getId).toList()));
        task.setStatus("RUNNING");
        task.setCaseCount(testCases.size());
        task.setFileCount(0);
        task.setCreateTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        automationScriptTaskMapper.insert(task);

        return task;
    }

    private String buildSystemPrompt() {
        return """
                你是一名资深测试开发工程师，擅长根据测试用例生成企业级 pytest + requests 接口自动化脚本。

                你必须遵守：
                1. 只输出 JSON，不要输出 Markdown，不要解释。
                2. 生成的脚本必须是 pytest + requests 风格。
                3. 不能凭空编造接口路径。如果测试用例里没有明确接口路径，要在脚本中使用 pytest.skip 标记 TODO。
                4. 公共配置放到 conftest.py。
                5. 至少生成 requirements.txt、pytest.ini、README.md、conftest.py、tests/test_generated_cases.py。
                6. 每个测试用例生成一个 test_ 开头的方法。
                7. 断言要尽量根据 expectedResult 生成。
                8. token、base_url 等配置必须支持环境变量。
                9. 输出 JSON 格式如下：
                {
                  "files": [
                    {
                      "filePath": "requirements.txt",
                      "fileType": "TEXT",
                      "description": "依赖文件",
                      "content": "requests==..."
                    }
                  ]
                }
                """;
    }

    private String buildUserPrompt(
            AutomationScriptTask task,
            List<TestCase> testCases
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();

        payload.put("scriptFramework", "PYTEST_REQUESTS");
        payload.put("baseUrl", task.getBaseUrl());
        payload.put("authType", task.getAuthType());
        payload.put("authHeaderName", task.getAuthHeaderName());
        payload.put("tokenPlaceholder", task.getTokenPlaceholder());
        payload.put("commonHeaders", task.getCommonHeaders());
        payload.put("projectId", task.getProjectId());
        payload.put("versionNo", task.getVersionNo());
        payload.put("moduleCode", task.getModuleCode());

        List<Map<String, Object>> cases = new ArrayList<>();

        for (TestCase testCase : testCases) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", testCase.getId());
            item.put("moduleCode", testCase.getModuleCode());
            item.put("moduleName", testCase.getModuleName());
            item.put("caseTitle", testCase.getCaseTitle());
            item.put("caseType", testCase.getCaseType());
            item.put("priority", testCase.getPriority());
            item.put("precondition", testCase.getPrecondition());
            item.put("steps", testCase.getSteps());
            item.put("expectedResult", testCase.getExpectedResult());
            item.put("testData", testCase.getTestData());
            item.put("riskPoint", testCase.getRiskPoint());
            item.put("automationSuggestion", testCase.getAutomationSuggestion());
            cases.add(item);
        }

        payload.put("testCases", cases);

        return """
                请根据下面的测试用例生成 pytest + requests 接口自动化脚本。
                没有明确接口 path / method 的用例，请生成 pytest.skip，不要编造接口。
                返回 JSON，字段必须是 files。
                
                输入数据：
                """
                + toJson(payload);
    }

    private List<AiAutomationScriptFileDTO> buildTemplateFiles(
            AutomationScriptTask task,
            List<TestCase> testCases
    ) {
        List<AiAutomationScriptFileDTO> files = new ArrayList<>();

        files.add(file(
                "requirements.txt",
                "TEXT",
                "Python 依赖",
                """
                pytest>=8.0.0
                requests>=2.31.0
                python-dotenv>=1.0.0
                """
        ));

        files.add(file(
                "pytest.ini",
                "CONFIG",
                "pytest 配置",
                """
                [pytest]
                testpaths = tests
                python_files = test_*.py
                python_classes = Test*
                python_functions = test_*
                addopts = -s -v
                """
        ));

        files.add(file(
                "README.md",
                "MARKDOWN",
                "使用说明",
                buildReadme(task)
        ));

        files.add(file(
                "conftest.py",
                "PYTHON",
                "pytest 公共配置",
                buildConftest(task)
        ));

        files.add(file(
                "tests/test_generated_cases.py",
                "PYTHON",
                "自动生成的测试用例脚本",
                buildTemplateTestFile(testCases)
        ));

        return files;
    }

    private List<AiAutomationScriptFileDTO> ensureRequiredFiles(
            List<AiAutomationScriptFileDTO> files,
            AutomationScriptTask task,
            List<TestCase> testCases
    ) {
        List<AiAutomationScriptFileDTO> result = new ArrayList<>(files);

        if (!containsFile(result, "requirements.txt")) {
            result.add(file(
                    "requirements.txt",
                    "TEXT",
                    "Python 依赖",
                    """
                    pytest>=8.0.0
                    requests>=2.31.0
                    python-dotenv>=1.0.0
                    """
            ));
        }

        if (!containsFile(result, "pytest.ini")) {
            result.add(file(
                    "pytest.ini",
                    "CONFIG",
                    "pytest 配置",
                    """
                    [pytest]
                    testpaths = tests
                    python_files = test_*.py
                    python_classes = Test*
                    python_functions = test_*
                    addopts = -s -v
                    """
            ));
        }

        if (!containsFile(result, "README.md")) {
            result.add(file(
                    "README.md",
                    "MARKDOWN",
                    "使用说明",
                    buildReadme(task)
            ));
        }

        if (!containsFile(result, "conftest.py")) {
            result.add(file(
                    "conftest.py",
                    "PYTHON",
                    "pytest 公共配置",
                    buildConftest(task)
            ));
        }

        if (!hasTestsFile(result)) {
            result.add(file(
                    "tests/test_generated_cases.py",
                    "PYTHON",
                    "兜底测试脚本",
                    buildTemplateTestFile(testCases)
            ));
        }

        return result;
    }

    private boolean containsFile(List<AiAutomationScriptFileDTO> files, String filePath) {
        for (AiAutomationScriptFileDTO file : files) {
            if (filePath.equals(file.getFilePath())) {
                return true;
            }
        }

        return false;
    }

    private boolean hasTestsFile(List<AiAutomationScriptFileDTO> files) {
        for (AiAutomationScriptFileDTO file : files) {
            if (file.getFilePath() != null
                    && file.getFilePath().startsWith("tests/")
                    && file.getFilePath().endsWith(".py")) {
                return true;
            }
        }

        return false;
    }

    private String buildReadme(AutomationScriptTask task) {
        return """
                # API 自动化测试脚本
                
                ## 生成信息
                
                - 脚本任务ID：%s
                - 来源类型：%s
                - 项目ID：%s
                - 版本号：%s
                - 模块编码：%s
                - 框架：pytest + requests
                
                ## 安装依赖
                
                ```bash
                python3 -m venv .venv
                source .venv/bin/activate
                pip install -r requirements.txt
                ```
                
                ## 执行测试
                
                ```bash
                export API_BASE_URL="%s"
                export API_TOKEN="your-token"
                pytest
                ```
                
                ## 说明
                
                如果测试用例中没有明确接口 path / method，脚本会用 pytest.skip 标记 TODO，避免编造接口。
                """.formatted(
                task.getScriptTaskId(),
                task.getSourceType(),
                task.getProjectId(),
                task.getVersionNo(),
                task.getModuleCode(),
                task.getBaseUrl()
        );
    }

    private String buildConftest(AutomationScriptTask task) {
        return """
                import os
                import pytest
                import requests
                
                
                @pytest.fixture(scope="session")
                def base_url():
                    return os.getenv("API_BASE_URL", "%s").rstrip("/")
                
                
                @pytest.fixture(scope="session")
                def api_token():
                    return os.getenv("API_TOKEN", "")
                
                
                @pytest.fixture()
                def api_client(base_url, api_token):
                    session = requests.Session()
                    session.headers.update({
                        "Content-Type": "application/json"
                    })
                
                    if api_token:
                        session.headers.update({
                            "%s": "Bearer " + api_token
                        })
                
                    session.base_url = base_url
                    return session
                """.formatted(
                escapePython(task.getBaseUrl()),
                escapePython(resolveAuthHeaderName(task))
        );
    }

    private String buildTemplateTestFile(List<TestCase> testCases) {
        StringBuilder builder = new StringBuilder();

        builder.append("""
                import pytest
                
                
                """);

        int index = 1;

        for (TestCase testCase : testCases) {
            builder.append("@pytest.mark.generated\n");
            builder.append("def test_case_")
                    .append(testCase.getId())
                    .append("_")
                    .append(index++)
                    .append("(api_client):\n");

            builder.append("    \"\"\"\n");
            builder.append("    用例标题：")
                    .append(safeComment(testCase.getCaseTitle()))
                    .append("\n");
            builder.append("    模块：")
                    .append(safeComment(testCase.getModuleName()))
                    .append("\n");
            builder.append("    优先级：")
                    .append(safeComment(testCase.getPriority()))
                    .append("\n");
            builder.append("    前置条件：")
                    .append(safeComment(testCase.getPrecondition()))
                    .append("\n");
            builder.append("    测试步骤：")
                    .append(safeComment(testCase.getSteps()))
                    .append("\n");
            builder.append("    预期结果：")
                    .append(safeComment(testCase.getExpectedResult()))
                    .append("\n");
            builder.append("    \"\"\"\n");

            builder.append("    pytest.skip(\"TODO: 当前测试用例没有明确接口 method/path，请补充接口信息后实现请求和断言\")\n\n\n");
        }

        return builder.toString();
    }

    private AiAutomationScriptFileDTO file(
            String filePath,
            String fileType,
            String description,
            String content
    ) {
        AiAutomationScriptFileDTO file = new AiAutomationScriptFileDTO();

        file.setFilePath(filePath);
        file.setFileType(fileType);
        file.setDescription(description);
        file.setContent(content);

        return file;
    }

    private int saveFiles(
            String scriptTaskId,
            List<AiAutomationScriptFileDTO> files
    ) {
        int count = 0;

        for (AiAutomationScriptFileDTO fileDTO : files) {
            String filePath = normalizeFilePath(fileDTO.getFilePath());
            String content = fileDTO.getContent();

            AutomationScriptFile file = new AutomationScriptFile();
            file.setScriptTaskId(scriptTaskId);
            file.setFilePath(filePath);
            file.setFileType(defaultText(fileDTO.getFileType(), guessFileType(filePath)));
            file.setDescription(defaultText(fileDTO.getDescription(), ""));
            file.setFileContent(content);
            file.setFileSize(content == null ? 0 : content.getBytes(StandardCharsets.UTF_8).length);
            file.setCreateTime(LocalDateTime.now());
            file.setUpdateTime(LocalDateTime.now());

            automationScriptFileMapper.insert(file);
            count++;
        }

        return count;
    }

    private String normalizeFilePath(String filePath) {
        if (!StringUtils.hasText(filePath)) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "文件路径不能为空"
            );
        }

        String path = filePath.trim().replace("\\", "/");

        if (path.startsWith("/")) {
            path = path.substring(1);
        }

        if (path.contains("..")) {
            throw new BusinessException(
                    ErrorCode.PARAMS_ERROR,
                    "文件路径非法：" + filePath
            );
        }

        return path;
    }

    private String guessFileType(String filePath) {
        String lower = filePath.toLowerCase();

        if (lower.endsWith(".py")) {
            return "PYTHON";
        }

        if (lower.endsWith(".md")) {
            return "MARKDOWN";
        }

        if (lower.endsWith(".ini")
                || lower.endsWith(".yml")
                || lower.endsWith(".yaml")
                || lower.endsWith(".toml")) {
            return "CONFIG";
        }

        return "TEXT";
    }

    private AutomationScriptTask getTask(String scriptTaskId) {
        AutomationScriptTask task =
                automationScriptTaskMapper.selectOne(
                        new LambdaQueryWrapper<AutomationScriptTask>()
                                .eq(AutomationScriptTask::getScriptTaskId, scriptTaskId)
                                .last("LIMIT 1")
                );

        if (task == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "自动化脚本任务不存在"
            );
        }

        return task;
    }

    private void markFailed(AutomationScriptTask task, String errorMessage) {
        task.setStatus("FAILED");
        task.setErrorMessage(errorMessage);
        task.setUpdateTime(LocalDateTime.now());

        automationScriptTaskMapper.updateById(task);
    }

    private String normalizeSourceType(String sourceType) {
        if (!StringUtils.hasText(sourceType)) {
            return "CASE_SET";
        }

        String value = sourceType.trim().toUpperCase();

        return switch (value) {
            case "CASE_SET", "TASK", "IDS" -> value;
            default -> "CASE_SET";
        };
    }

    private String normalizeGenerateMode(String generateMode) {
        if (!StringUtils.hasText(generateMode)) {
            return "LLM";
        }

        String value = generateMode.trim().toUpperCase();

        return switch (value) {
            case "LLM", "TEMPLATE" -> value;
            default -> "LLM";
        };
    }

    private String normalizeAuthType(String authType) {
        if (!StringUtils.hasText(authType)) {
            return "NONE";
        }

        String value = authType.trim().toUpperCase();

        return switch (value) {
            case "NONE", "BEARER_TOKEN", "CUSTOM_HEADER" -> value;
            default -> "NONE";
        };
    }

    private String resolveAuthHeaderName(AutomationScriptTask task) {
        if (StringUtils.hasText(task.getAuthHeaderName())) {
            return task.getAuthHeaderName();
        }

        if ("CUSTOM_HEADER".equals(task.getAuthType())) {
            return "X-Token";
        }

        return "Authorization";
    }

    private String defaultText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value : defaultValue;
    }

    private String escapePython(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }

    private String safeComment(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }

        return value
                .replace("\"\"\"", "\\\"\\\"\\\"")
                .replace("\r", " ")
                .replace("\n", " ");
    }

    private String toJson(Object object) {
        try {
            if (object == null) {
                return "null";
            }

            return objectMapper.writeValueAsString(object);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "JSON 序列化失败：" + e.getMessage()
            );
        }
    }

    private AutomationScriptTaskVO toTaskVO(AutomationScriptTask task) {
        AutomationScriptTaskVO vo = new AutomationScriptTaskVO();

        vo.setId(task.getId());
        vo.setScriptTaskId(task.getScriptTaskId());
        vo.setSourceType(task.getSourceType());
        vo.setCaseSetId(task.getCaseSetId());
        vo.setTestcaseTaskId(task.getTestcaseTaskId());
        vo.setProjectId(task.getProjectId());
        vo.setVersionNo(task.getVersionNo());
        vo.setModuleCode(task.getModuleCode());
        vo.setScriptFramework(task.getScriptFramework());
        vo.setGenerateMode(task.getGenerateMode());
        vo.setBaseUrl(task.getBaseUrl());
        vo.setAuthType(task.getAuthType());
        vo.setAuthHeaderName(task.getAuthHeaderName());
        vo.setTokenPlaceholder(task.getTokenPlaceholder());
        vo.setCommonHeaders(task.getCommonHeaders());
        vo.setSelectedCaseIds(task.getSelectedCaseIds());
        vo.setStatus(task.getStatus());
        vo.setCaseCount(task.getCaseCount());
        vo.setFileCount(task.getFileCount());
        vo.setRawModelOutput(task.getRawModelOutput());
        vo.setErrorMessage(task.getErrorMessage());
        vo.setSummary(task.getSummary());
        vo.setCreateTime(task.getCreateTime());
        vo.setUpdateTime(task.getUpdateTime());

        return vo;
    }

    private AutomationScriptFileVO toFileVO(AutomationScriptFile file) {
        AutomationScriptFileVO vo = new AutomationScriptFileVO();

        vo.setId(file.getId());
        vo.setScriptTaskId(file.getScriptTaskId());
        vo.setFilePath(file.getFilePath());
        vo.setFileType(file.getFileType());
        vo.setDescription(file.getDescription());
        vo.setFileContent(file.getFileContent());
        vo.setFileSize(file.getFileSize());
        vo.setCreateTime(file.getCreateTime());
        vo.setUpdateTime(file.getUpdateTime());

        return vo;
    }
}