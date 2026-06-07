package com.zy.testpilotai.automation.model.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AutomationScriptFileVO {

    private Long id;

    private String scriptTaskId;

    private String filePath;

    private String fileType;

    private String description;

    private String fileContent;

    private Integer fileSize;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}