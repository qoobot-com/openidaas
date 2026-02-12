package com.qoobot.openidaas.organization.converter;

import com.qoobot.openidaas.organization.entity.Organization;
import com.qoobot.openidaas.organization.vo.OrganizationVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 组织转换器
 *
 * @author Qoobot
 * @version 1.0.0
 */
@Mapper
public interface OrganizationConverter {

    OrganizationConverter INSTANCE = Mappers.getMapper(OrganizationConverter.class);

    @Mapping(target = "managerName", ignore = true)
    OrganizationVO toVO(Organization organization);

    List<OrganizationVO> toVOList(List<Organization> organizations);

}