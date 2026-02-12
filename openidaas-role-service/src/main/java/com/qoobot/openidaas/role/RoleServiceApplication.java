package com.qoobot.openidaas.role;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 角色服务启动类
 *
 * @author QooBot
 */
@SpringBootApplication(scanBasePackages = "com.qoobot.openidaas")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.qoobot.openidaas.common.feign")
public class RoleServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoleServiceApplication.class, args);
    }
}
