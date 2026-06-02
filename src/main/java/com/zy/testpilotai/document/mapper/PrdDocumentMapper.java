package com.zy.testpilotai.document.mapper;

import com.zy.testpilotai.document.model.entity.PrdDocumentEntity;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface PrdDocumentMapper {

    int insert(PrdDocumentEntity document);

    PrdDocumentEntity selectById(@Param("id") Long id);

    List<PrdDocumentEntity> selectByProjectId(
            @Param("projectId") Long projectId,
            @Param("versionName") String versionName
    );

    int logicDeleteById(@Param("id") Long id);
}