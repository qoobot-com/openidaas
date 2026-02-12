package com.qoobot.openidaas.organization.controller;

import com.qoobot.openidaas.common.dto.position.PositionCreateDTO;
import com.qoobot.openidaas.common.dto.position.PositionUpdateDTO;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.common.vo.position.PositionVO;
import com.qoobot.openidaas.organization.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 职位管理Controller
 *
 * @author QooBot
 */
@Slf4j
@RestController
@RequestMapping("/api/organizations/positions")
@RequiredArgsConstructor
@Tag(name = "职位管理", description = "职位管理相关接口")
public class PositionController {

    private final PositionService positionService;

    /**
     * 获取职位列表
     */
    @GetMapping
    @Operation(summary = "获取职位列表", description = "查询职位信息列表")
    public ResultVO<List<PositionVO>> getPositionList(
            @Parameter(description = "部门ID筛选") @RequestParam(required = false) Long deptId) {
        List<PositionVO> positions = positionService.getPositionList(deptId);
        return ResultVO.success(positions);
    }

    /**
     * 创建职位
     */
    @PostMapping
    @Operation(summary = "创建职位", description = "创建新的职位")
    public ResultVO<PositionVO> createPosition(@Valid @RequestBody PositionCreateDTO createDTO) {
        PositionVO positionVO = positionService.createPosition(createDTO);
        return ResultVO.success(positionVO);
    }

    /**
     * 获取职位详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取职位详情", description = "根据职位ID获取职位详细信息")
    public ResultVO<PositionVO> getPositionById(@Parameter(description = "职位ID") @PathVariable Long id) {
        PositionVO positionVO = positionService.getPositionById(id);
        return ResultVO.success(positionVO);
    }

    /**
     * 更新职位 - 符合OpenAPI规范，使用请求体
     */
    @PutMapping
    @Operation(summary = "更新职位", description = "更新职位信息")
    public ResultVO<PositionVO> updatePosition(@Valid @RequestBody PositionUpdateDTO updateDTO) {
        PositionVO positionVO = positionService.updatePosition(updateDTO);
        return ResultVO.success(positionVO);
    }

    /**
     * 删除职位 - 符合RESTful规范，使用路径参数
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除职位", description = "删除指定职位（需确保无用户使用）")
    public ResultVO<Void> deletePosition(@Parameter(description = "职位ID") @PathVariable Long id) {
        positionService.deletePosition(id);
        return ResultVO.success();
    }
}
