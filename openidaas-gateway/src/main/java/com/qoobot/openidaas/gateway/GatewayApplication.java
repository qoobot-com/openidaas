package com.qoobot.openidaas.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * OpenIDaaS API 网关启动类
 * 
 * 基于 Spring Cloud Gateway 实现 API 网关
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@SpringBootApplication
public class GatewayApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
