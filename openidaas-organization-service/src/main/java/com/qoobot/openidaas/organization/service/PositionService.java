package com.qoobot.openidaas.organization.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qoobot.openidaas.common.dto.position.PositionCreateDTO;
import com.qoobot.openidaas.common.dto.position.PositionUpdateDTO;
import com.qoobot.openidaas.common.vo.position.PositionVO;
import com.qoobot.openidaas.organization.entity.Position;

import java.util.List;

/**
 * 职位服务接口
 *
 * @author QooBot
 */
public interface PositionService extends IService<Position> {

    /**
     * 获取职位列表
     *
     * @param deptId 部门ID（可选）
     * @return 职位列表
     */
    List<PositionVO> getPositionList(Long deptId);

    /**
     * 创建职位
     *
     * @param createDTO 创建职位DTO
     * @return 职位VO
     */
    PositionVO createPosition(PositionCreateDTO createDTO);

    /**
     * 更新职位
     *
     * @param updateDTO 更新职位DTO
     * @return 职位VO
     */
    PositionVO updatePosition(PositionUpdateDTO updateDTO);

    /**
     * 删除职位
     *
     * @param id 职位ID
     */
    void deletePosition(Long id);

    /**
     * 获取职位详情
     *
     * @param id 职位ID
     * @return 职位VO
     */
    PositionVO getPositionById(Long id);
}
