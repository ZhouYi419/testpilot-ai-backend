package com.zy.testpilotai.automation.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("automation_script_file")
public class AutomationScriptFile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 脚本生成任务业务 ID。
     */
    private String scriptTaskId;

    /**
     * 文件路径。
     */
    private String filePath;

    /**
     * 文件类型：
     * PYTHON / TEXT / CONFIG / MARKDOWN。
     */
    private String fileType;

    /**
     * 文件说明。
     */
    private String description;

    /**
     * 文件内容。
     */
    private String fileContent;

    /**
     * 文件大小。
     */
    private Integer fileSize;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}