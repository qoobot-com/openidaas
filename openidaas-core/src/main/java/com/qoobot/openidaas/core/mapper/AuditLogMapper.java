package com.qoobot.openidaas.core.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.core.domain.AuditLog;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志Mapper接口
 *
 * @author QooBot
 */
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    /**
     * 根据操作类型查找审计日志（分页）
     */
    IPage<AuditLog> findByOperationType(Page<AuditLog> page, @Param("operationType") String operationType);

    /**
     * 根据模块查找审计日志（分页）
     */
    IPage<AuditLog> findByModule(Page<AuditLog> page, @Param("module") String module);

    /**
     * 根据操作用户ID查找审计日志（分页）
     */
    IPage<AuditLog> findByOperatorId(Page<AuditLog> page, @Param("operatorId") Long operatorId);

    /**
     * 根据租户ID查找审计日志（分页）
     */
    IPage<AuditLog> findByTenantId(Page<AuditLog> page, @Param("tenantId") Long tenantId);

    /**
     * 根据应用ID查找审计日志（分页）
     */
    IPage<AuditLog> findByAppId(Page<AuditLog> page, @Param("appId") Long appId);

    /**
     * 根据操作结果查找审计日志（分页）
     */
    IPage<AuditLog> findByResult(Page<AuditLog> page, @Param("result") String result);

    /**
     * 根据时间范围查找审计日志（分页）
     */
    IPage<AuditLog> findByOperationTimeBetween(Page<AuditLog> page,
                                                 @Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime);

    /**
     * 根据操作用户ID和时间范围查找审计日志（分页）
     */
    IPage<AuditLog> findByOperatorIdAndOperationTimeBetween(Page<AuditLog> page,
                                                             @Param("operatorId") Long operatorId,
                                                             @Param("startTime") LocalDateTime startTime,
                                                             @Param("endTime") LocalDateTime endTime);

    /**
     * 根据租户ID和时间范围查找审计日志（分页）
     */
    IPage<AuditLog> findByTenantIdAndOperationTimeBetween(Page<AuditLog> page,
                                                           @Param("tenantId") Long tenantId,
                                                           @Param("startTime") LocalDateTime startTime,
                                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 统计指定时间范围内的操作次数
     */
    @Select("SELECT COUNT(*) FROM audit_logs WHERE operation_time BETWEEN #{startTime} AND #{endTime}")
    long countByOperationTimeBetween(@Param("startTime") LocalDateTime startTime,
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户操作次数
     */
    @Select("SELECT COUNT(*) FROM audit_logs WHERE operator_id = #{operatorId}")
    long countByOperatorId(@Param("operatorId") Long operatorId);

    /**
     * 统计模块操作次数
     * 【需在XML中实现】涉及GROUP BY聚合
     */
    List<Object[]> countByModuleAndOperationTimeBetween(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 统计操作类型分布
     * 【需在XML中实现】涉及GROUP BY聚合
     */
    List<Object[]> countByOperationTypeAndOperationTimeBetween(@Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的审计日志
     */
    @Delete("DELETE FROM audit_logs WHERE operation_time < #{beforeTime}")
    int deleteByOperationTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 查找最近的操作日志（分页）
     */
    @Select("SELECT * FROM audit_logs ORDER BY operation_time DESC")
    IPage<AuditLog> findRecentLogs(Page<AuditLog> page);

    /**
     * 查找失败的操作日志（分页）
     */
    @Select("SELECT * FROM audit_logs WHERE result = 'FAIL' ORDER BY operation_time DESC")
    IPage<AuditLog> findFailedLogs(Page<AuditLog> page);
}
