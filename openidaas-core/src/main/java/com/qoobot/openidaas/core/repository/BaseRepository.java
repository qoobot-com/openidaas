package com.qoobot.openidaas.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

/**
 * 基础仓储接口
 *
 * @param <T> 实体类型
 * @param <ID> 主键类型
 * @author QooBot
 */
@NoRepositoryBean
public interface BaseRepository<T, ID extends Serializable> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    
    /**
     * 根据ID和租户ID查找实体
     */
    T findByIdAndTenantId(ID id, Long tenantId);
    
    /**
     * 检查实体是否存在（根据ID和租户ID）
     */
    boolean existsByIdAndTenantId(ID id, Long tenantId);
    
    /**
     * 根据ID和租户ID删除实体
     */
    void deleteByIdAndTenantId(ID id, Long tenantId);
}