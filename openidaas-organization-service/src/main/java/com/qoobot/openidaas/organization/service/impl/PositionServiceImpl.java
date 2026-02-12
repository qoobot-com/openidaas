package com.qoobot.openidaas.organization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qoobot.openidaas.common.dto.position.PositionCreateDTO;
import com.qoobot.openidaas.common.dto.position.PositionUpdateDTO;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.vo.position.PositionVO;
import com.qoobot.openidaas.organization.entity.Department;
import com.qoobot.openidaas.organization.entity.Position;
import com.qoobot.openidaas.organization.mapper.DepartmentMapper;
import com.qoobot.openidaas.organization.mapper.PositionMapper;
import com.qoobot.openidaas.organization.service.PositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 职位服务实现类
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PositionServiceImpl extends ServiceImpl<PositionMapper, Position> implements PositionService {

    private final PositionMapper positionMapper;
    private final DepartmentMapper departmentMapper;

    @Override
    public List<PositionVO> getPositionList(Long deptId) {
        List<Position> positions;
        if (deptId == null) {
            // 查询所有职位
            LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
            wrapper.orderByAsc(Position::getLevel);
            positions = positionMapper.selectList(wrapper);
        } else {
            // 查询指定部门的职位
            positions = positionMapper.selectByDeptId(deptId);
        }
        return positions.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PositionVO createPosition(PositionCreateDTO createDTO) {
        log.info("创建职位，职位编码：{}，职位名称：{}", createDTO.getPositionCode(), createDTO.getPositionName());

        // 检查职位编码是否已存在
        Position existPosition = positionMapper.selectByCode(createDTO.getPositionCode());
        if (existPosition != null) {
            throw new BusinessException("职位编码已存在：" + createDTO.getPositionCode());
        }

        // 验证部门是否存在
        if (createDTO.getDeptId() != null) {
            Department department = departmentMapper.selectById(createDTO.getDeptId());
            if (department == null) {
                throw new BusinessException("部门不存在");
            }
        }

        // 验证汇报对象是否存在
        if (createDTO.getReportsTo() != null) {
            Position reportsToPosition = positionMapper.selectById(createDTO.getReportsTo());
            if (reportsToPosition == null) {
                throw new BusinessException("汇报对象职位不存在");
            }
        }

        // 构建职位实体
        Position position = new Position();
        BeanUtils.copyProperties(createDTO, position);

        // 设置默认值
        if (position.getIsManager() == null) {
            position.setIsManager(0);
        }
        if (position.getLevel() == null) {
            position.setLevel(1);
        }

        positionMapper.insert(position);

        log.info("职位创建成功，职位ID：{}", position.getId());
        return convertToVO(position);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PositionVO updatePosition(PositionUpdateDTO updateDTO) {
        log.info("更新职位，职位ID：{}", updateDTO.getId());

        // 检查职位是否存在
        Position existPosition = positionMapper.selectById(updateDTO.getId());
        if (existPosition == null) {
            throw new BusinessException("职位不存在");
        }

        // 检查职位编码是否重复（排除自己）
        Position codePosition = positionMapper.selectByCode(updateDTO.getPositionCode());
        if (codePosition != null && !codePosition.getId().equals(updateDTO.getId())) {
            throw new BusinessException("职位编码已存在：" + updateDTO.getPositionCode());
        }

        // 验证部门是否存在
        if (updateDTO.getDeptId() != null) {
            Department department = departmentMapper.selectById(updateDTO.getDeptId());
            if (department == null) {
                throw new BusinessException("部门不存在");
            }
        }

        // 验证汇报对象是否存在（且不能是自己）
        if (updateDTO.getReportsTo() != null) {
            if (updateDTO.getReportsTo().equals(updateDTO.getId())) {
                throw new BusinessException("汇报对象不能是自己");
            }
            Position reportsToPosition = positionMapper.selectById(updateDTO.getReportsTo());
            if (reportsToPosition == null) {
                throw new BusinessException("汇报对象职位不存在");
            }
        }

        // 更新职位信息
        Position position = new Position();
        BeanUtils.copyProperties(updateDTO, position);

        positionMapper.updateById(position);

        log.info("职位更新成功，职位ID：{}", updateDTO.getId());
        return convertToVO(positionMapper.selectById(updateDTO.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePosition(Long id) {
        log.info("删除职位，职位ID：{}", id);

        // 检查职位是否存在
        Position position = positionMapper.selectById(id);
        if (position == null) {
            throw new BusinessException("职位不存在");
        }

        // 检查是否有用户使用该职位
        int userCount = positionMapper.countUsers(id);
        if (userCount > 0) {
            throw new BusinessException("职位下存在用户，无法删除");
        }

        positionMapper.deleteById(id);

        log.info("职位删除成功，职位ID：{}", id);
    }

    @Override
    public PositionVO getPositionById(Long id) {
        Position position = positionMapper.selectById(id);
        if (position == null) {
            throw new BusinessException("职位不存在");
        }
        return convertToVO(position);
    }

    /**
     * 转换为VO
     */
    private PositionVO convertToVO(Position position) {
        PositionVO vo = new PositionVO();
        BeanUtils.copyProperties(position, vo);
        vo.setManager(position.getIsManager() == 1);

        // 加载部门名称
        if (position.getDeptId() != null) {
            Department dept = departmentMapper.selectById(position.getDeptId());
            if (dept != null) {
                vo.setDeptName(dept.getDeptName());
            }
        }

        // 加载汇报对象名称
        if (position.getReportsTo() != null) {
            Position reportsTo = positionMapper.selectById(position.getReportsTo());
            if (reportsTo != null) {
                vo.setReportsToName(reportsTo.getPositionName());
            }
        }

        return vo;
    }
}
