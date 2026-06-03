package com.zy.testpilotai.testcase.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zy.testpilotai.ai.llm.LlmClient;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeSearchResultVO;
import com.zy.testpilotai.knowledge.service.KnowledgeSearchService;
import com.zy.testpilotai.project.mapper.ProjectMapper;
import com.zy.testpilotai.project.model.entity.ProjectEntity;
import com.zy.testpilotai.testcase.mapper.TestCaseMapper;
import com.zy.testpilotai.testcase.mapper.TestcaseGenerationTaskMapper;
import com.zy.testpilotai.testcase.model.dto.AiGeneratedTestCaseDTO;
import com.zy.testpilotai.testcase.model.dto.TestCaseGenerateRequest;
import com.zy.testpilotai.testcase.model.entity.TestCaseEntity;
import com.zy.testpilotai.testcase.model.entity.TestcaseGenerationTaskEntity;
import com.zy.testpilotai.testcase.model.vo.TestCaseGenerateResponseVO;
import com.zy.testpilotai.testcase.model.vo.TestCaseVO;
import com.zy.testpilotai.testcase.service.TestCaseGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TestCaseGenerationServiceImpl implements TestCaseGenerationService {

    private final ProjectMapper projectMapper;

    private final KnowledgeSearchService knowledgeSearchService;

    private final LlmClient llmClient;

    private final ObjectMapper objectMapper;

    private final TestcaseGenerationTaskMapper taskMapper;

    private final TestCaseMapper testCaseMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TestCaseGenerateResponseVO generate(TestCaseGenerateRequest request) {
        validateRequest(request);

        ProjectEntity project = projectMapper.selectById(request.getProjectId());
        if (project == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "项目不存在");
        }

        TestcaseGenerationTaskEntity task = createTask(request);

        try {
            taskMapper.updateStatus(task.getId(), "RUNNING", 0, llmClient.getModelName(), null);

            List<KnowledgeSearchResultVO> searchResults = searchRequirementChunks(request);
            if (searchResults.isEmpty()) {
                throw new BusinessException(ErrorCode.KNOWLEDGE_BASE_ERROR, "知识库未召回相关 PRD 内容，无法生成测试用例");
            }

            String systemPrompt = buildSystemPrompt();
            String userPrompt = buildUserPrompt(request, searchResults);

            String llmResult = llmClient.chat(systemPrompt, userPrompt);
            String json = extractJsonArray(llmResult);

            List<AiGeneratedTestCaseDTO> generatedCases = objectMapper.readValue(
                    json,
                    new TypeReference<List<AiGeneratedTestCaseDTO>>() {
                    }
            );

            if (generatedCases == null || generatedCases.isEmpty()) {
                throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI 未生成有效测试用例");
            }

            String sourceChunkIds = searchResults.stream()
                    .map(item -> String.valueOf(item.getChunkId()))
                    .collect(Collectors.joining(","));

            for (AiGeneratedTestCaseDTO generatedCase : generatedCases) {
                TestCaseEntity entity = toEntity(request, task.getId(), generatedCase, sourceChunkIds);
                testCaseMapper.insert(entity);
            }

            taskMapper.updateStatus(task.getId(), "SUCCESS", generatedCases.size(), llmClient.getModelName(), null);

            List<TestCaseVO> records = testCaseMapper.selectByTaskId(task.getId())
                    .stream()
                    .map(this::toVO)
                    .toList();

            TestCaseGenerateResponseVO response = new TestCaseGenerateResponseVO();
            response.setTaskId(task.getId());
            response.setTotalCases(records.size());
            response.setTestCases(records);
            return response;
        } catch (BusinessException e) {
            taskMapper.updateStatus(task.getId(), "FAILED", 0, llmClient.getModelName(), e.getMessage());
            throw e;
        } catch (Exception e) {
            taskMapper.updateStatus(task.getId(), "FAILED", 0, llmClient.getModelName(), e.getMessage());
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "生成测试用例失败：" + e.getMessage());
        }
    }

    @Override
    public List<TestCaseVO> listByProject(Long projectId, String versionName, String moduleName) {
        if (projectId == null || projectId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "项目ID不合法");
        }

        ProjectEntity project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "项目不存在");
        }

        String queryVersionName = StringUtils.hasText(versionName) ? versionName.trim() : null;
        String queryModuleName = StringUtils.hasText(moduleName) ? moduleName.trim() : null;

        return testCaseMapper.selectByProject(projectId, queryVersionName, queryModuleName)
                .stream()
                .map(this::toVO)
                .toList();
    }

    private void validateRequest(TestCaseGenerateRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }

        if (request.getProjectId() == null || request.getProjectId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "项目ID不合法");
        }

        if (!StringUtils.hasText(request.getRequirementText())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成需求不能为空");
        }

        if (request.getRequirementText().trim().length() > 2000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "生成需求不能超过2000个字符");
        }
    }

    private TestcaseGenerationTaskEntity createTask(TestCaseGenerateRequest request) {
        TestcaseGenerationTaskEntity task = new TestcaseGenerationTaskEntity();
        task.setProjectId(request.getProjectId());
        task.setVersionName(request.getVersionName());
        task.setModuleName(request.getModuleName());
        task.setRequirementText(request.getRequirementText());
        task.setStatus("PENDING");
        task.setTotalCases(0);
        task.setModelName(llmClient.getModelName());
        task.setErrorMessage(null);

        int rows = taskMapper.insert(task);
        if (rows <= 0 || task.getId() == null) {
            throw new BusinessException(ErrorCode.DATABASE_ERROR, "创建测试用例生成任务失败");
        }

        return task;
    }

    private List<KnowledgeSearchResultVO> searchRequirementChunks(TestCaseGenerateRequest request) {
        KnowledgeSearchRequest searchRequest = new KnowledgeSearchRequest();
        searchRequest.setProjectId(request.getProjectId());
        searchRequest.setVersionName(request.getVersionName());

        String query = request.getRequirementText();
        if (StringUtils.hasText(request.getModuleName())) {
            query = request.getModuleName() + "\n" + query;
        }

        searchRequest.setQuery(query);
        searchRequest.setTopK(request.getTopK() == null ? 5 : request.getTopK());
        searchRequest.setMinSimilarity(null);

        return knowledgeSearchService.search(searchRequest);
    }

    private String buildSystemPrompt() {
        return """
                你是一个资深测试专家，擅长根据 PRD 设计完整、可执行、可验证的测试用例。

                你必须严格遵守以下规则：
                1. 只能基于提供的 PRD 知识片段生成测试用例。
                2. 不要编造 PRD 中不存在的功能。
                3. 测试用例必须可执行、可验证。
                4. 每条用例必须有清晰的前置条件、测试步骤和预期结果。
                5. 优先覆盖正常流程、异常流程、边界值、权限校验、安全性、数据一致性。
                6. 输出必须是合法 JSON 数组。
                7. 不要输出 Markdown。
                8. 不要输出 ```json 代码块。
                9. 不要输出任何解释说明，只输出 JSON 数组。
                10. 回答必须使用中文。
                """;
    }

    private String buildUserPrompt(TestCaseGenerateRequest request, List<KnowledgeSearchResultVO> searchResults) {
        String caseTypes = request.getCaseTypes() == null || request.getCaseTypes().isEmpty()
                ? "正常流程、异常流程、边界值、权限校验、安全性、数据一致性"
                : String.join("、", request.getCaseTypes());

        String context = buildContext(searchResults);

        return """
                用户生成目标：
                %s

                模块名称：
                %s

                期望生成数量：
                %d

                希望覆盖的用例类型：
                %s

                PRD 知识片段：
                %s

                请输出 JSON 数组，格式必须如下：
                [
                  {
                    "caseTitle": "用例标题",
                    "moduleName": "所属模块",
                    "priority": "P0/P1/P2/P3",
                    "caseType": "正常流程/异常流程/边界值/安全测试/数据一致性",
                    "precondition": "前置条件",
                    "steps": ["步骤1", "步骤2", "步骤3"],
                    "expectedResult": "预期结果",
                    "testData": "测试数据，必须是字符串。如果有多个字段，请写成一段中文说明，不要返回对象"
                  }
                ]
                """.formatted(
                request.getRequirementText(),
                StringUtils.hasText(request.getModuleName()) ? request.getModuleName() : "未指定",
                request.getCount() == null ? 10 : request.getCount(),
                caseTypes,
                context
        );
    }

    private String buildContext(List<KnowledgeSearchResultVO> searchResults) {
        StringBuilder context = new StringBuilder();

        int index = 1;
        for (KnowledgeSearchResultVO result : searchResults) {
            context.append("\n【片段").append(index).append("】\n")
                    .append("chunkId: ").append(result.getChunkId()).append("\n")
                    .append("documentId: ").append(result.getDocumentId()).append("\n")
                    .append("versionName: ").append(result.getVersionName()).append("\n")
                    .append("title: ").append(result.getTitle()).append("\n")
                    .append("chunkType: ").append(result.getChunkType()).append("\n")
                    .append("sectionPath: ").append(result.getSectionPath()).append("\n")
                    .append("moduleName: ").append(result.getModuleName()).append("\n")
                    .append("requirementId: ").append(result.getRequirementId()).append("\n")
                    .append("similarity: ").append(result.getSimilarity()).append("\n")
                    .append("content:\n")
                    .append(result.getContent())
                    .append("\n");
            index++;
        }

        return context.toString();
    }

    /**
     * 兼容模型偶尔返回 ```json ... ``` 的情况。
     */
    private String extractJsonArray(String rawText) {
        if (!StringUtils.hasText(rawText)) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI 返回内容为空");
        }

        String text = rawText.trim();

        if (text.startsWith("```")) {
            text = text.replaceAll("^```json", "")
                    .replaceAll("^```", "")
                    .replaceAll("```$", "")
                    .trim();
        }

        int start = text.indexOf("[");
        int end = text.lastIndexOf("]");

        if (start < 0 || end < 0 || end <= start) {
            throw new BusinessException(ErrorCode.AI_SERVICE_ERROR, "AI 返回内容不是 JSON 数组：" + rawText);
        }

        return text.substring(start, end + 1);
    }

    private TestCaseEntity toEntity(
            TestCaseGenerateRequest request,
            Long taskId,
            AiGeneratedTestCaseDTO generatedCase,
            String sourceChunkIds
    ) {
        TestCaseEntity entity = new TestCaseEntity();
        entity.setProjectId(request.getProjectId());
        entity.setTaskId(taskId);
        entity.setVersionName(request.getVersionName());
        entity.setModuleName(resolveModuleName(request, generatedCase));
        entity.setCaseTitle(resolveText(generatedCase.getCaseTitle(), "未命名测试用例"));
        entity.setPriority(resolveText(generatedCase.getPriority(), "P2"));
        entity.setCaseType(resolveText(generatedCase.getCaseType(), "功能测试"));
        entity.setPrecondition(resolveText(generatedCase.getPrecondition(), ""));
        entity.setSteps(joinSteps(generatedCase.getSteps()));
        entity.setExpectedResult(resolveText(generatedCase.getExpectedResult(), ""));
        entity.setTestData(resolveObjectText(generatedCase.getTestData()));
        entity.setSourceChunkIds(sourceChunkIds);
        entity.setAiScore(BigDecimal.valueOf(80));
        entity.setReviewStatus("UNREVIEWED");
        return entity;
    }

    private String resolveModuleName(TestCaseGenerateRequest request, AiGeneratedTestCaseDTO generatedCase) {
        if (StringUtils.hasText(generatedCase.getModuleName())) {
            return generatedCase.getModuleName();
        }
        if (StringUtils.hasText(request.getModuleName())) {
            return request.getModuleName();
        }
        return "未分类模块";
    }

    private String resolveText(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim() : defaultValue;
    }

    private String resolveObjectText(Object value) {
        if (value == null) {
            return "";
        }

        if (value instanceof String stringValue) {
            return StringUtils.hasText(stringValue) ? stringValue.trim() : "";
        }

        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    private String joinSteps(List<String> steps) {
        if (steps == null || steps.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) {
            builder.append(i + 1).append(". ").append(steps.get(i)).append("\n");
        }
        return builder.toString().trim();
    }

    private TestCaseVO toVO(TestCaseEntity entity) {
        TestCaseVO vo = new TestCaseVO();
        vo.setId(entity.getId());
        vo.setProjectId(entity.getProjectId());
        vo.setTaskId(entity.getTaskId());
        vo.setVersionName(entity.getVersionName());
        vo.setModuleName(entity.getModuleName());
        vo.setCaseTitle(entity.getCaseTitle());
        vo.setPriority(entity.getPriority());
        vo.setCaseType(entity.getCaseType());
        vo.setPrecondition(entity.getPrecondition());
        vo.setSteps(entity.getSteps());
        vo.setExpectedResult(entity.getExpectedResult());
        vo.setTestData(entity.getTestData());
        vo.setSourceChunkIds(entity.getSourceChunkIds());
        vo.setAiScore(entity.getAiScore());
        vo.setReviewStatus(entity.getReviewStatus());
        vo.setCreatedAt(entity.getCreatedAt());
        vo.setUpdatedAt(entity.getUpdatedAt());
        return vo;
    }
}