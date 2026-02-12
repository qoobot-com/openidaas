package com.qoobot.openidaas.audit.converter;

import com.qoobot.openidaas.audit.entity.AuditLog;
import com.qoobot.openidaas.common.dto.audit.AuditLogCreateDTO;
import com.qoobot.openidaas.common.vo.audit.AuditLogVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * 审计日志转换器
 *
 * @author QooBot
 */
@Mapper(componentModel = "spring")
public interface AuditLogConverter {

    /**
     * 创建DTO转实体
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    AuditLog toEntity(AuditLogCreateDTO dto);

    /**
     * 实体转VO
     */
    AuditLogVO toVO(AuditLog entity);
}
