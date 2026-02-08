package com.qoobot.openidaas.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OpenIDaaS 认证服务启动类
 * 
 * 提供 OAuth2.1/OIDC 认证和授权服务
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.qoobot.openidaas"})
public class OpenIdaasAuthApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OpenIdaasAuthApplication.class, args);
    }
}
