package com.zy.testpilotai.automation.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("automation_case_result")
public class AutomationCaseResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 自动化执行任务业务 ID。
     */
    private String runTaskId;

    /**
     * 测试类名。
     */
    private String className;

    /**
     * 测试方法名。
     */
    private String caseName;

    /**
     * 状态：
     * PASSED / FAILED / ERROR / SKIPPED。
     */
    private String status;

    /**
     * 耗时秒。
     */
    private Double timeSeconds;

    /**
     * 失败 / 错误 / 跳过说明。
     */
    private String message;

    /**
     * 失败详情。
     */
    private String detail;

    /**
     * system-out。
     */
    private String systemOut;

    /**
     * system-err。
     */
    private String systemErr;

    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}