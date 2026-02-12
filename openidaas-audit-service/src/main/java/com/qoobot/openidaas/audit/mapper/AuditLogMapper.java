package com.qoobot.openidaas.audit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qoobot.openidaas.audit.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 审计日志Mapper
 *
 * @author QooBot
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    /**
     * 根据操作类型查询审计日志
     */
    IPage<AuditLog> selectByOperationType(
            @Param("operationType") String operationType,
            Page<AuditLog> page);

    /**
     * 根据模块查询审计日志
     */
    IPage<AuditLog> selectByModule(
            @Param("module") String module,
            Page<AuditLog> page);

    /**
     * 根据操作人ID查询审计日志
     */
    IPage<AuditLog> selectByOperatorId(
            @Param("operatorId") Long operatorId,
            Page<AuditLog> page);

    /**
     * 根据租户ID查询审计日志
     */
    IPage<AuditLog> selectByTenantId(
            @Param("tenantId") Long tenantId,
            Page<AuditLog> page);

    /**
     * 根据应用ID查询审计日志
     */
    IPage<AuditLog> selectByAppId(
            @Param("appId") Long appId,
            Page<AuditLog> page);

    /**
     * 根据操作结果查询审计日志
     */
    IPage<AuditLog> selectByResult(
            @Param("result") String result,
            Page<AuditLog> page);

    /**
     * 根据时间范围查询审计日志
     */
    IPage<AuditLog> selectByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Page<AuditLog> page);

    /**
     * 统计指定时间范围内的操作次数
     */
    @Select("SELECT COUNT(*) FROM audit_logs WHERE operation_time BETWEEN #{startTime} AND #{endTime}")
    long countByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户操作次数
     */
    @Select("SELECT COUNT(*) FROM audit_logs WHERE operator_id = #{operatorId}")
    long countByOperatorId(@Param("operatorId") Long operatorId);

    /**
     * 统计模块操作分布
     */
    @Select("SELECT module, COUNT(*) as count FROM audit_logs " +
            "WHERE operation_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY module ORDER BY count DESC")
    List<Map<String, Object>> countByModuleAndTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计操作类型分布
     */
    @Select("SELECT operation_type, COUNT(*) as count FROM audit_logs " +
            "WHERE operation_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY operation_type ORDER BY count DESC")
    List<Map<String, Object>> countByOperationTypeAndTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计操作结果分布
     */
    @Select("SELECT result, COUNT(*) as count FROM audit_logs " +
            "WHERE operation_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY result")
    List<Map<String, Object>> countByResultAndTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * 统计热门操作用户
     */
    @Select("SELECT operator_id, operator_name, COUNT(*) as count FROM audit_logs " +
            "WHERE operation_time BETWEEN #{startTime} AND #{endTime} " +
            "GROUP BY operator_id, operator_name " +
            "ORDER BY count DESC LIMIT #{limit}")
    List<Map<String, Object>> countTopOperatorsByTimeRange(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("limit") int limit);

    /**
     * 删除指定时间之前的审计日志
     */
    @Select("DELETE FROM audit_logs WHERE operation_time < #{beforeTime}")
    int deleteByOperationTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 查找最近的操作日志
     */
    IPage<AuditLog> selectRecentLogs(Page<AuditLog> page);

    /**
     * 查找失败的操作日志
     */
    IPage<AuditLog> selectFailedLogs(Page<AuditLog> page);
}
