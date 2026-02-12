package com.qoobot.openidaas.core.repository;

import com.qoobot.openidaas.core.domain.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志仓储接口
 *
 * @author QooBot
 */
public interface AuditLogRepository extends BaseRepository<AuditLog, Long> {

    /**
     * 根据操作类型查找审计日志
     */
    Page<AuditLog> findByOperationType(String operationType, Pageable pageable);

    /**
     * 根据模块查找审计日志
     */
    Page<AuditLog> findByModule(String module, Pageable pageable);

    /**
     * 根据操作用户ID查找审计日志
     */
    Page<AuditLog> findByOperatorId(Long operatorId, Pageable pageable);

    /**
     * 根据租户ID查找审计日志
     */
    Page<AuditLog> findByTenantId(Long tenantId, Pageable pageable);

    /**
     * 根据应用ID查找审计日志
     */
    Page<AuditLog> findByAppId(Long appId, Pageable pageable);

    /**
     * 根据操作结果查找审计日志
     */
    Page<AuditLog> findByResult(String result, Pageable pageable);

    /**
     * 根据时间范围查找审计日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.operationTime BETWEEN :startTime AND :endTime")
    Page<AuditLog> findByOperationTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                             @Param("endTime") LocalDateTime endTime, 
                                             Pageable pageable);

    /**
     * 根据操作用户ID和时间范围查找审计日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.operatorId = :operatorId AND a.operationTime BETWEEN :startTime AND :endTime")
    Page<AuditLog> findByOperatorIdAndOperationTimeBetween(@Param("operatorId") Long operatorId,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime,
                                                          Pageable pageable);

    /**
     * 根据租户ID和时间范围查找审计日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.tenantId = :tenantId AND a.operationTime BETWEEN :startTime AND :endTime")
    Page<AuditLog> findByTenantIdAndOperationTimeBetween(@Param("tenantId") Long tenantId,
                                                        @Param("startTime") LocalDateTime startTime,
                                                        @Param("endTime") LocalDateTime endTime,
                                                        Pageable pageable);

    /**
     * 统计指定时间范围内的操作次数
     */
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.operationTime BETWEEN :startTime AND :endTime")
    long countByOperationTimeBetween(@Param("startTime") LocalDateTime startTime, 
                                   @Param("endTime") LocalDateTime endTime);

    /**
     * 统计用户操作次数
     */
    long countByOperatorId(Long operatorId);

    /**
     * 统计模块操作次数
     */
    @Query("SELECT a.module, COUNT(a) FROM AuditLog a WHERE a.operationTime BETWEEN :startTime AND :endTime GROUP BY a.module")
    List<Object[]> countByModuleAndOperationTimeBetween(@Param("startTime") LocalDateTime startTime,
                                                       @Param("endTime") LocalDateTime endTime);

    /**
     * 统计操作类型分布
     */
    @Query("SELECT a.operationType, COUNT(a) FROM AuditLog a WHERE a.operationTime BETWEEN :startTime AND :endTime GROUP BY a.operationType")
    List<Object[]> countByOperationTypeAndOperationTimeBetween(@Param("startTime") LocalDateTime startTime,
                                                              @Param("endTime") LocalDateTime endTime);

    /**
     * 删除指定时间之前的审计日志
     */
    @Modifying
    @Query("DELETE FROM AuditLog a WHERE a.operationTime < :beforeTime")
    int deleteByOperationTimeBefore(@Param("beforeTime") LocalDateTime beforeTime);

    /**
     * 查找最近的操作日志
     */
    @Query("SELECT a FROM AuditLog a ORDER BY a.operationTime DESC")
    Page<AuditLog> findRecentLogs(Pageable pageable);

    /**
     * 查找失败的操作日志
     */
    @Query("SELECT a FROM AuditLog a WHERE a.result = 'FAIL' ORDER BY a.operationTime DESC")
    Page<AuditLog> findFailedLogs(Pageable pageable);
}