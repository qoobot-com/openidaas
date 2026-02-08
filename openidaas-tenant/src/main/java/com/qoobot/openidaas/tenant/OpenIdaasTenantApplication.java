package com.qoobot.openidaas.tenant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OpenIDaaS 租户服务启动类
 * 
 * 提供租户管理功能
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.qoobot.openidaas"})
public class OpenIdaasTenantApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OpenIdaasTenantApplication.class, args);
    }
}
