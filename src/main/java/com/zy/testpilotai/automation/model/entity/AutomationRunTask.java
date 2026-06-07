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
@TableName(value = "automation_run_task", autoResultMap = true)
public class AutomationRunTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 自动化执行任务业务 ID。
     */
    private String runTaskId;

    /**
     * 脚本生成任务业务 ID。
     */
    private String scriptTaskId;

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
     * 执行环境名称。
     */
    private String environmentName;

    /**
     * 执行模式：
     * LOCAL_PROCESS / DOCKER_SANDBOX。
     */
    private String executionMode;

    /**
     * 工作目录。
     */
    private String workDir;

    /**
     * 报告文件路径。
     */
    private String reportFilePath;

    /**
     * 基础 URL。
     */
    private String baseUrl;

    /**
     * API Token。
     */
    private String apiToken;

    /**
     * 额外环境变量 JSON。
     */
    @TableField(jdbcType = JdbcType.OTHER, typeHandler = JsonbTypeHandler.class)
    private String extraEnv;

    /**
     * 超时时间，单位秒。
     */
    private Integer timeoutSeconds;

    /**
     * 是否请求取消：
     * 0：否
     * 1：是。
     */
    private Integer cancelRequested;

    /**
     * 状态：
     * PENDING / RUNNING / SUCCESS / FAILED / CANCELLED。
     */
    private String status;

    /**
     * pytest 退出码。
     */
    private Integer exitCode;

    /**
     * 总测试数。
     */
    private Integer totalCount;

    /**
     * 通过数。
     */
    private Integer passedCount;

    /**
     * 失败数。
     */
    private Integer failedCount;

    /**
     * 错误数。
     */
    private Integer errorCount;

    /**
     * 跳过数。
     */
    private Integer skippedCount;

    /**
     * 耗时毫秒。
     */
    private Long durationMs;

    /**
     * 标准输出。
     */
    private String stdoutLog;

    /**
     * 标准错误。
     */
    private String stderrLog;

    /**
     * JUnit XML 原文。
     */
    private String junitXml;

    /**
     * 错误信息。
     */
    private String errorMessage;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;

    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;

    private LocalDateTime startTime;

    private LocalDateTime endTime;
}