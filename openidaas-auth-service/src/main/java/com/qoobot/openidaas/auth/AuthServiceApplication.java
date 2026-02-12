package com.qoobot.openidaas.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 认证服务启动类
 *
 * @author QooBot
 */
@SpringBootApplication(scanBasePackages = "com.qoobot.openidaas")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.qoobot.openidaas.common.feign", "com.qoobot.openidaas.auth.client"})
public class AuthServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
