package com.qoobot.openidaas.user.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA配置类
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.qoobot.openidaas.user.repository")
@EnableJpaAuditing
@EnableTransactionManagement
public class JpaConfig {
}
