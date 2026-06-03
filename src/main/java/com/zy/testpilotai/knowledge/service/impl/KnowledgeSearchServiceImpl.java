package com.zy.testpilotai.knowledge.service.impl;

import com.zy.testpilotai.ai.embedding.EmbeddingClient;
import com.zy.testpilotai.ai.embedding.VectorUtils;
import com.zy.testpilotai.common.exception.BusinessException;
import com.zy.testpilotai.common.exception.ErrorCode;
import com.zy.testpilotai.knowledge.mapper.KnowledgeChunkMapper;
import com.zy.testpilotai.knowledge.model.dto.KnowledgeSearchRequest;
import com.zy.testpilotai.knowledge.model.entity.KnowledgeChunkEntity;
import com.zy.testpilotai.knowledge.model.vo.KnowledgeSearchResultVO;
import com.zy.testpilotai.knowledge.service.KnowledgeSearchService;
import com.zy.testpilotai.project.mapper.ProjectMapper;
import com.zy.testpilotai.project.model.entity.ProjectEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KnowledgeSearchServiceImpl implements KnowledgeSearchService {

    private final ProjectMapper projectMapper;

    private final KnowledgeChunkMapper knowledgeChunkMapper;

    private final EmbeddingClient embeddingClient;

    @Override
    public List<KnowledgeSearchResultVO> search(KnowledgeSearchRequest request) {
        validateRequest(request);

        ProjectEntity project = projectMapper.selectById(request.getProjectId());
        if (project == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "项目不存在");
        }

        Integer topK = request.getTopK() == null ? 5 : request.getTopK();
        if (topK < 1) {
            topK = 5;
        }
        if (topK > 20) {
            topK = 20;
        }

        String versionName = StringUtils.hasText(request.getVersionName())
                ? request.getVersionName().trim()
                : null;

        List<Double> queryVector = embeddingClient.embed(request.getQuery().trim());
        String queryEmbedding = VectorUtils.toPgVectorString(queryVector);

        List<KnowledgeChunkEntity> chunks = knowledgeChunkMapper.searchSimilarChunks(
                request.getProjectId(),
                versionName,
                queryEmbedding,
                topK
        );

        Double minSimilarity = request.getMinSimilarity();

        return chunks.stream()
                .map(this::toSearchResultVO)
                .filter(vo -> minSimilarity == null || vo.getSimilarity() == null || vo.getSimilarity() >= minSimilarity)
                .toList();
    }

    private void validateRequest(KnowledgeSearchRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数不能为空");
        }

        if (request.getProjectId() == null || request.getProjectId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "项目ID不合法");
        }

        if (!StringUtils.hasText(request.getQuery())) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询内容不能为空");
        }

        if (request.getQuery().trim().length() > 2000) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "查询内容不能超过2000个字符");
        }
    }

    private KnowledgeSearchResultVO toSearchResultVO(KnowledgeChunkEntity entity) {
        KnowledgeSearchResultVO vo = new KnowledgeSearchResultVO();
        vo.setChunkId(entity.getId());
        vo.setProjectId(entity.getProjectId());
        vo.setDocumentId(entity.getDocumentId());
        vo.setVersionName(entity.getVersionName());
        vo.setChunkIndex(entity.getChunkIndex());
        vo.setTitle(entity.getTitle());
        vo.setContent(entity.getContent());

        vo.setChunkType(entity.getChunkType());
        vo.setSectionPath(entity.getSectionPath());
        vo.setModuleName(entity.getModuleName());
        vo.setRequirementId(entity.getRequirementId());

        vo.setDistance(entity.getDistance());

        if (entity.getDistance() != null) {
            vo.setSimilarity(1 - entity.getDistance());
        }

        vo.setMetadata(entity.getMetadata());
        return vo;
    }
}