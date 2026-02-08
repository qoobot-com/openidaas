package com.qoobot.openidaas.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OpenIDaaS 用户服务启动类
 * 
 * 提供用户管理功能
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.qoobot.openidaas"})
public class OpenIdaasUserApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(OpenIdaasUserApplication.class, args);
    }
}
