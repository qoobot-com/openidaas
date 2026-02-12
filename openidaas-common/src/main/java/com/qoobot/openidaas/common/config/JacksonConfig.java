package com.qoobot.openidaas.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Jackson配置类
 *
 * @author QooBot
 */
@AutoConfiguration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        // 注册JavaTime模块以支持LocalDateTime等时间类型
        mapper.registerModule(new JavaTimeModule());
        return mapper;
    }
}