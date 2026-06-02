package com.zy.testpilotai.document.mapper;

import com.zy.testpilotai.document.model.entity.PrdDocumentEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * PRD 文档 Mapper
 */
@Mapper
public interface PrdDocumentMapper {

    int insert(PrdDocumentEntity document);

    PrdDocumentEntity selectById(@Param("id") Long id);

    List<PrdDocumentEntity> selectByProjectId(
            @Param("projectId") Long projectId,
            @Param("versionName") String versionName
    );

    int updateParseStatus(
            @Param("id") Long id,
            @Param("parseStatus") String parseStatus,
            @Param("errorMessage") String errorMessage
    );

    int updateParsedContent(
            @Param("id") Long id,
            @Param("parseStatus") String parseStatus,
            @Param("parsedContent") String parsedContent,
            @Param("errorMessage") String errorMessage
    );

    int updateIndexStatus(
            @Param("id") Long id,
            @Param("indexStatus") String indexStatus,
            @Param("errorMessage") String errorMessage
    );

    int logicDeleteById(@Param("id") Long id);
}