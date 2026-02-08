package com.qoobot.openidaas.user.repository;

import com.qoobot.openidaas.user.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户档案数据访问接口
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Long> {

    /**
     * 根据用户ID查找档案
     */
    Optional<UserProfile> findByUserId(Long userId);

    /**
     * 检查用户档案是否存在
     */
    boolean existsByUserId(Long userId);
}
