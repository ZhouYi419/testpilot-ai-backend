package com.zy.testpilotai.aieval.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.LocalDateTime;

@Data
@TableName(value = "ai_eval_app_config", autoResultMap = true)
public class AiEvalAppConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 待测 AI 应用配置业务 ID。
     */
    private String appConfigId;

    /**
     * 配置名称。
     */
    private String configName;

    /**
     * 应用类型：
     * RAG / LLM / AGENT / PROMPT / MIXED。
     */
    private String appType;

    /**
     * 接口地址。
     */
    private String endpointUrl;

    /**
     * HTTP 方法。
     */
    private String httpMethod;

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
     * API Key。
     */
    private String apiKey;

    /**
     * 请求 Headers JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String headers;

    /**
     * 请求体模板。
     */
    private String requestBodyTemplate;

    /**
     * 响应 JSON 路径。
     */
    private String responseJsonPath;

    /**
     * 超时时间，单位秒。
     */
    private Integer timeoutSeconds;

    /**
     * 描述。
     */
    private String description;

    /**
     * 状态：
     * ACTIVE / DELETED。
     */
    private String status;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}