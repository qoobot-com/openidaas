package com.qoobot.openidaas.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 核心配置类
 *
 * @author QooBot
 */
@Configuration
public class CoreConfig {

    /**
     * ObjectMapper Bean
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}