package com.qoobot.openidaas.organization;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * OpenIDaaS组织服务启动类
 *
 * @author Qoobot
 * @version 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@ComponentScan(basePackages = "com.qoobot.openidaas")
@MapperScan("com.qoobot.openidaas.organization.mapper")
public class OpenIDaaSOrganizationApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenIDaaSOrganizationApplication.class, args);
    }

}