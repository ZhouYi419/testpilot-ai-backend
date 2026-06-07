package com.zy.testpilotai.automation.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.zy.testpilotai.common.typehandler.JsonbTypeHandler;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import java.time.LocalDateTime;

@Data
@TableName(value = "automation_script_task", autoResultMap = true)
public class AutomationScriptTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 脚本生成任务业务 ID。
     */
    private String scriptTaskId;

    /**
     * 来源类型：
     * CASE_SET / TASK / IDS。
     */
    private String sourceType;

    /**
     * 用例集 ID。
     */
    private String caseSetId;

    /**
     * 测试用例生成任务 ID。
     */
    private String testcaseTaskId;

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
     * 脚本框架。
     */
    private String scriptFramework;

    /**
     * 生成模式：
     * LLM / TEMPLATE。
     */
    private String generateMode;

    /**
     * 基础请求地址。
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
     * 公共 Header JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String commonHeaders;

    /**
     * 选中的测试用例 ID JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String selectedCaseIds;

    /**
     * 状态：
     * RUNNING / SUCCESS / FAILED。
     */
    private String status;

    /**
     * 测试用例数量。
     */
    private Integer caseCount;

    /**
     * 文件数量。
     */
    private Integer fileCount;

    /**
     * 模型原始输出。
     */
    private String rawModelOutput;

    /**
     * 错误信息。
     */
    private String errorMessage;

    /**
     * 汇总 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String summary;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}