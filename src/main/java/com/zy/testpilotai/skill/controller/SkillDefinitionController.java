package com.zy.testpilotai.skill.controller;

import com.zy.testpilotai.common.response.BaseResponse;
import com.zy.testpilotai.common.response.ResultUtils;
import com.zy.testpilotai.skill.model.dto.SkillCreateRequest;
import com.zy.testpilotai.skill.model.dto.SkillUpdateRequest;
import com.zy.testpilotai.skill.model.vo.SkillDefinitionVO;
import com.zy.testpilotai.skill.service.SkillDefinitionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillDefinitionController {

    private final SkillDefinitionService skillDefinitionService;

    /**
     * 初始化系统内置 Skill。
     */
    @PostMapping("/init-builtin")
    public BaseResponse<List<SkillDefinitionVO>> initBuiltin() {
        return ResultUtils.success(skillDefinitionService.initBuiltinSkills());
    }

    /**
     * 创建自定义 Skill。
     */
    @PostMapping("/create")
    public BaseResponse<SkillDefinitionVO> create(
            @RequestBody @Valid SkillCreateRequest request
    ) {
        return ResultUtils.success(skillDefinitionService.create(request));
    }

    /**
     * 更新 Skill。
     */
    @PutMapping("/update")
    public BaseResponse<SkillDefinitionVO> update(
            @RequestBody @Valid SkillUpdateRequest request
    ) {
        return ResultUtils.success(skillDefinitionService.update(request));
    }

    /**
     * 查询 Skill 列表。
     */
    @GetMapping("/list")
    public BaseResponse<List<SkillDefinitionVO>> list(
            @RequestParam(required = false, defaultValue = "false") Boolean enabledOnly
    ) {
        return ResultUtils.success(skillDefinitionService.list(enabledOnly));
    }

    /**
     * 根据 Skill 编码查询详情。
     */
    @GetMapping("/{skillCode}")
    public BaseResponse<SkillDefinitionVO> getByCode(
            @PathVariable String skillCode
    ) {
        return ResultUtils.success(skillDefinitionService.getByCode(skillCode));
    }

    /**
     * 启用 Skill。
     */
    @PostMapping("/{skillCode}/enable")
    public BaseResponse<Boolean> enable(
            @PathVariable String skillCode
    ) {
        return ResultUtils.success(skillDefinitionService.enable(skillCode));
    }

    /**
     * 禁用 Skill。
     */
    @PostMapping("/{skillCode}/disable")
    public BaseResponse<Boolean> disable(
            @PathVariable String skillCode
    ) {
        return ResultUtils.success(skillDefinitionService.disable(skillCode));
    }
}