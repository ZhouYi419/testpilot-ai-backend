package com.zy.testpilotai.automation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.zy.testpilotai.automation.mapper.AutomationCaseResultMapper;
import com.zy.testpilotai.automation.mapper.AutomationRunTaskMapper;
import com.zy.testpilotai.automation.mapper.AutomationScriptFileMapper;
import com.zy.testpilotai.automation.model.entity.AutomationCaseResult;
import com.zy.testpilotai.automation.model.entity.AutomationRunTask;
import com.zy.testpilotai.automation.model.entity.AutomationScriptFile;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class AutomationRunAsyncProcessor {

    private final AutomationRunTaskMapper automationRunTaskMapper;

    private final AutomationScriptFileMapper automationScriptFileMapper;

    private final AutomationCaseResultMapper automationCaseResultMapper;

    private final ObjectMapper objectMapper;

    /**
     * 自动化执行工作目录。
     * 默认放在项目目录 runtime/automation-runs 下。
     */
    @Value("${automation.execution.work-dir:runtime/automation-runs}")
    private String automationWorkDir;

    /**
     * Python 命令。
     * Mac / Linux 默认 python3。
     */
    @Value("${automation.execution.python-command:python3}")
    private String pythonCommand;

    /**
     * 是否创建 venv。
     */
    @Value("${automation.execution.create-venv:true}")
    private boolean createVenv;

    /**
     * 当前正在执行的进程。
     */
    private final Map<String, Process> runningProcesses = new ConcurrentHashMap<>();

    /**
     * 异步执行自动化任务。
     */
    @Async
    public void execute(String runTaskId) {
        AutomationRunTask task = getRunTask(runTaskId);
        long startMillis = System.currentTimeMillis();

        try {
            markRunning(task);

            List<AutomationScriptFile> scriptFiles = loadScriptFiles(task.getScriptTaskId());

            if (scriptFiles.isEmpty()) {
                throw new BusinessException(
                        ErrorCode.NOT_FOUND_ERROR,
                        "脚本文件不存在，无法执行"
                );
            }

            Path workDir = prepareWorkDir(task);
            writeScriptFiles(workDir, scriptFiles);

            Path reportsDir = workDir.resolve("reports");
            Files.createDirectories(reportsDir);

            Path junitXmlPath = reportsDir.resolve("junit.xml");

            Map<String, String> env = buildEnv(task);

            StringBuilder stdoutBuilder = new StringBuilder();
            StringBuilder stderrBuilder = new StringBuilder();

            checkCancel(runTaskId);

            if (createVenv) {
                ProcessResult venvResult = runCommand(
                        runTaskId,
                        List.of(pythonCommand, "-m", "venv", ".venv"),
                        workDir.toFile(),
                        env,
                        task.getTimeoutSeconds()
                );

                stdoutBuilder.append(venvResult.stdout());
                stderrBuilder.append(venvResult.stderr());

                if (venvResult.timedOut()) {
                    throw new BusinessException(
                            ErrorCode.SYSTEM_ERROR,
                            "创建 Python 虚拟环境超时"
                    );
                }

                if (venvResult.exitCode() != 0) {
                    throw new BusinessException(
                            ErrorCode.SYSTEM_ERROR,
                            "创建 Python 虚拟环境失败，exitCode=" + venvResult.exitCode()
                    );
                }
            }

            checkCancel(runTaskId);

            String pythonExecutable = resolvePythonExecutable(workDir);

            if (Files.exists(workDir.resolve("requirements.txt"))) {
                ProcessResult installResult = runCommand(
                        runTaskId,
                        List.of(
                                pythonExecutable,
                                "-m",
                                "pip",
                                "install",
                                "-r",
                                "requirements.txt"
                        ),
                        workDir.toFile(),
                        env,
                        task.getTimeoutSeconds()
                );

                stdoutBuilder.append(installResult.stdout());
                stderrBuilder.append(installResult.stderr());

                if (installResult.timedOut()) {
                    throw new BusinessException(
                            ErrorCode.SYSTEM_ERROR,
                            "安装 Python 依赖超时"
                    );
                }

                if (installResult.exitCode() != 0) {
                    throw new BusinessException(
                            ErrorCode.SYSTEM_ERROR,
                            "安装 Python 依赖失败，exitCode=" + installResult.exitCode()
                    );
                }
            }

            checkCancel(runTaskId);

            ProcessResult pytestResult = runCommand(
                    runTaskId,
                    List.of(
                            pythonExecutable,
                            "-m",
                            "pytest",
                            "--junitxml=" + junitXmlPath.toString()
                    ),
                    workDir.toFile(),
                    env,
                    task.getTimeoutSeconds()
            );

            stdoutBuilder.append(pytestResult.stdout());
            stderrBuilder.append(pytestResult.stderr());

            if (pytestResult.timedOut()) {
                throw new BusinessException(
                        ErrorCode.SYSTEM_ERROR,
                        "pytest 执行超时"
                );
            }

            JunitParseResult junitParseResult = parseJunitXml(runTaskId, junitXmlPath);

            String junitXml = Files.exists(junitXmlPath)
                    ? Files.readString(junitXmlPath, StandardCharsets.UTF_8)
                    : null;

            task = getRunTask(runTaskId);

            if (task.getCancelRequested() != null && task.getCancelRequested() == 1) {
                markCancelled(task, stdoutBuilder.toString(), stderrBuilder.toString());
                return;
            }

            boolean success = pytestResult.exitCode() == 0
                    && junitParseResult.failedCount() == 0
                    && junitParseResult.errorCount() == 0;

            task.setStatus(success ? "SUCCESS" : "FAILED");
            task.setExitCode(pytestResult.exitCode());
            task.setTotalCount(junitParseResult.totalCount());
            task.setPassedCount(junitParseResult.passedCount());
            task.setFailedCount(junitParseResult.failedCount());
            task.setErrorCount(junitParseResult.errorCount());
            task.setSkippedCount(junitParseResult.skippedCount());
            task.setDurationMs(System.currentTimeMillis() - startMillis);
            task.setStdoutLog(limitText(stdoutBuilder.toString()));
            task.setStderrLog(limitText(stderrBuilder.toString()));
            task.setJunitXml(junitXml);
            task.setReportFilePath(junitXmlPath.toString());
            task.setEndTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());

            if (!success) {
                task.setErrorMessage("pytest 执行完成，但存在失败或错误用例");
            }

            automationRunTaskMapper.updateById(task);
        } catch (BusinessException e) {
            markFailed(runTaskId, e.getMessage(), startMillis);
        } catch (Exception e) {
            markFailed(runTaskId, "自动化执行失败：" + e.getMessage(), startMillis);
        } finally {
            runningProcesses.remove(runTaskId);
        }
    }

    /**
     * 取消正在运行的进程。
     */
    public void cancelProcess(String runTaskId) {
        Process process = runningProcesses.get(runTaskId);

        if (process != null && process.isAlive()) {
            process.destroyForcibly();
        }
    }

    private void markRunning(AutomationRunTask task) {
        task.setStatus("RUNNING");
        task.setStartTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        automationRunTaskMapper.updateById(task);
    }

    private void markFailed(
            String runTaskId,
            String errorMessage,
            long startMillis
    ) {
        AutomationRunTask task = getRunTask(runTaskId);

        if (task.getCancelRequested() != null && task.getCancelRequested() == 1) {
            task.setStatus("CANCELLED");
        } else {
            task.setStatus("FAILED");
        }

        task.setErrorMessage(errorMessage);
        task.setDurationMs(System.currentTimeMillis() - startMillis);
        task.setEndTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        automationRunTaskMapper.updateById(task);
    }

    private void markCancelled(
            AutomationRunTask task,
            String stdout,
            String stderr
    ) {
        task.setStatus("CANCELLED");
        task.setStdoutLog(limitText(stdout));
        task.setStderrLog(limitText(stderr));
        task.setEndTime(LocalDateTime.now());
        task.setUpdateTime(LocalDateTime.now());

        automationRunTaskMapper.updateById(task);
    }

    private AutomationRunTask getRunTask(String runTaskId) {
        AutomationRunTask task = automationRunTaskMapper.selectOne(
                new LambdaQueryWrapper<AutomationRunTask>()
                        .eq(AutomationRunTask::getRunTaskId, runTaskId)
                        .last("LIMIT 1")
        );

        if (task == null) {
            throw new BusinessException(
                    ErrorCode.NOT_FOUND_ERROR,
                    "自动化执行任务不存在"
            );
        }

        return task;
    }

    private List<AutomationScriptFile> loadScriptFiles(String scriptTaskId) {
        return automationScriptFileMapper.selectList(
                new LambdaQueryWrapper<AutomationScriptFile>()
                        .eq(AutomationScriptFile::getScriptTaskId, scriptTaskId)
                        .orderByAsc(AutomationScriptFile::getId)
        );
    }

    private Path prepareWorkDir(AutomationRunTask task) throws Exception {
        Path rootDir = Path.of(automationWorkDir).toAbsolutePath().normalize();
        Path workDir = rootDir.resolve(task.getRunTaskId()).normalize();

        if (!workDir.startsWith(rootDir)) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "工作目录非法"
            );
        }

        Files.createDirectories(workDir);

        task.setWorkDir(workDir.toString());
        task.setUpdateTime(LocalDateTime.now());

        automationRunTaskMapper.updateById(task);

        return workDir;
    }

    private void writeScriptFiles(
            Path workDir,
            List<AutomationScriptFile> files
    ) throws Exception {
        for (AutomationScriptFile file : files) {
            Path targetPath = workDir.resolve(file.getFilePath()).normalize();

            if (!targetPath.startsWith(workDir)) {
                throw new BusinessException(
                        ErrorCode.SYSTEM_ERROR,
                        "脚本文件路径非法：" + file.getFilePath()
                );
            }

            Files.createDirectories(targetPath.getParent());
            Files.writeString(
                    targetPath,
                    file.getFileContent(),
                    StandardCharsets.UTF_8
            );
        }
    }

    private Map<String, String> buildEnv(AutomationRunTask task) {
        Map<String, String> env = new java.util.HashMap<>();

        env.put("API_BASE_URL", task.getBaseUrl() == null ? "" : task.getBaseUrl());
        env.put("API_TOKEN", task.getApiToken() == null ? "" : task.getApiToken());
        env.put("AUTOMATION_RUN_TASK_ID", task.getRunTaskId());

        if (StringUtils.hasText(task.getExtraEnv())
                && !"null".equals(task.getExtraEnv())) {
            try {
                Map<String, String> extraEnv = objectMapper.readValue(
                        task.getExtraEnv(),
                        new TypeReference<Map<String, String>>() {
                        }
                );

                env.putAll(extraEnv);
            } catch (Exception ignored) {
                // 额外环境变量解析失败时不阻塞执行。
            }
        }

        return env;
    }

    private String resolvePythonExecutable(Path workDir) {
        if (!createVenv) {
            return pythonCommand;
        }

        boolean windows = System.getProperty("os.name")
                .toLowerCase()
                .contains("win");

        if (windows) {
            return workDir.resolve(".venv")
                    .resolve("Scripts")
                    .resolve("python.exe")
                    .toString();
        }

        return workDir.resolve(".venv")
                .resolve("bin")
                .resolve("python")
                .toString();
    }

    private ProcessResult runCommand(
            String runTaskId,
            List<String> command,
            File workDir,
            Map<String, String> env,
            Integer timeoutSeconds
    ) throws Exception {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workDir);
        processBuilder.environment().putAll(env);

        Process process = processBuilder.start();
        runningProcesses.put(runTaskId, process);

        StreamCollector stdoutCollector = new StreamCollector(process.getInputStream());
        StreamCollector stderrCollector = new StreamCollector(process.getErrorStream());

        Thread stdoutThread = new Thread(stdoutCollector);
        Thread stderrThread = new Thread(stderrCollector);

        stdoutThread.start();
        stderrThread.start();

        int safeTimeout = timeoutSeconds == null || timeoutSeconds <= 0
                ? 600
                : timeoutSeconds;

        boolean finished = process.waitFor(safeTimeout, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            stdoutThread.join(1000);
            stderrThread.join(1000);

            return new ProcessResult(
                    -1,
                    stdoutCollector.content(),
                    stderrCollector.content(),
                    true
            );
        }

        stdoutThread.join(1000);
        stderrThread.join(1000);

        return new ProcessResult(
                process.exitValue(),
                stdoutCollector.content(),
                stderrCollector.content(),
                false
        );
    }

    private void checkCancel(String runTaskId) {
        AutomationRunTask task = getRunTask(runTaskId);

        if (task.getCancelRequested() != null && task.getCancelRequested() == 1) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "自动化执行任务已取消"
            );
        }
    }

    private JunitParseResult parseJunitXml(
            String runTaskId,
            Path junitXmlPath
    ) {
        if (!Files.exists(junitXmlPath)) {
            return new JunitParseResult(0, 0, 0, 0, 0);
        }

        try {
            Document document = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(junitXmlPath.toFile());

            document.getDocumentElement().normalize();

            List<Element> testcaseElements = findElements(document, "testcase");

            int total = 0;
            int passed = 0;
            int failed = 0;
            int errors = 0;
            int skipped = 0;

            for (Element testcaseElement : testcaseElements) {
                total++;

                AutomationCaseResult result = new AutomationCaseResult();

                result.setRunTaskId(runTaskId);
                result.setClassName(testcaseElement.getAttribute("classname"));
                result.setCaseName(testcaseElement.getAttribute("name"));
                result.setTimeSeconds(parseDouble(testcaseElement.getAttribute("time")));
                result.setCreateTime(LocalDateTime.now());

                Element failureElement = firstChildElement(testcaseElement, "failure");
                Element errorElement = firstChildElement(testcaseElement, "error");
                Element skippedElement = firstChildElement(testcaseElement, "skipped");
                Element systemOutElement = firstChildElement(testcaseElement, "system-out");
                Element systemErrElement = firstChildElement(testcaseElement, "system-err");

                if (failureElement != null) {
                    failed++;
                    result.setStatus("FAILED");
                    result.setMessage(failureElement.getAttribute("message"));
                    result.setDetail(failureElement.getTextContent());
                } else if (errorElement != null) {
                    errors++;
                    result.setStatus("ERROR");
                    result.setMessage(errorElement.getAttribute("message"));
                    result.setDetail(errorElement.getTextContent());
                } else if (skippedElement != null) {
                    skipped++;
                    result.setStatus("SKIPPED");
                    result.setMessage(skippedElement.getAttribute("message"));
                    result.setDetail(skippedElement.getTextContent());
                } else {
                    passed++;
                    result.setStatus("PASSED");
                }

                if (systemOutElement != null) {
                    result.setSystemOut(systemOutElement.getTextContent());
                }

                if (systemErrElement != null) {
                    result.setSystemErr(systemErrElement.getTextContent());
                }

                automationCaseResultMapper.insert(result);
            }

            return new JunitParseResult(total, passed, failed, errors, skipped);
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.SYSTEM_ERROR,
                    "解析 JUnit XML 失败：" + e.getMessage()
            );
        }
    }

    private List<Element> findElements(Document document, String tagName) {
        List<Element> result = new ArrayList<>();

        var nodeList = document.getElementsByTagName(tagName);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);

            if (node instanceof Element element) {
                result.add(element);
            }
        }

        return result;
    }

    private Element firstChildElement(Element parent, String tagName) {
        var nodeList = parent.getElementsByTagName(tagName);

        if (nodeList.getLength() <= 0) {
            return null;
        }

        Node node = nodeList.item(0);

        if (node instanceof Element element) {
            return element;
        }

        return null;
    }

    private Double parseDouble(String value) {
        if (!StringUtils.hasText(value)) {
            return 0.0;
        }

        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private String limitText(String text) {
        if (text == null) {
            return null;
        }

        int maxLength = 200_000;

        if (text.length() <= maxLength) {
            return text;
        }

        return text.substring(0, maxLength) + "\n...日志过长，已截断...";
    }

    private record ProcessResult(
            int exitCode,
            String stdout,
            String stderr,
            boolean timedOut
    ) {
    }

    private record JunitParseResult(
            int totalCount,
            int passedCount,
            int failedCount,
            int errorCount,
            int skippedCount
    ) {
    }

    private static class StreamCollector implements Runnable {

        private final java.io.InputStream inputStream;

        private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        private StreamCollector(java.io.InputStream inputStream) {
            this.inputStream = inputStream;
        }

        @Override
        public void run() {
            try (inputStream; outputStream) {
                inputStream.transferTo(outputStream);
            } catch (Exception ignored) {
            }
        }

        private String content() {
            return outputStream.toString(StandardCharsets.UTF_8);
        }
    }
}