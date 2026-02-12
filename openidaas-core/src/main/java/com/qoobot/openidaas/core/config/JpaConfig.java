package com.qoobot.openidaas.core.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * JPA配置类
 *
 * @author QooBot
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.qoobot.openidaas.core.repository")
@EntityScan(basePackages = "com.qoobot.openidaas.core.domain")
@EnableTransactionManagement
public class JpaConfig {
}