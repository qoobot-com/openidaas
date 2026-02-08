package com.qoobot.openidaas.auth.repository;

import com.qoobot.openidaas.auth.model.RegisteredClientEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * RegisteredClient Repository
 * 
 * OAuth2 客户端数据访问层
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Repository
public interface RegisteredClientRepository extends JpaRepository<RegisteredClientEntity, Long> {

    /**
     * 根据客户端 ID 查找
     */
    Optional<RegisteredClientEntity> findByClientId(String clientId);
}
