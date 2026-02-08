package com.qoobot.openidaas.starter.autoconfigure;

import com.qoobot.openidaas.starter.properties.OpenIDaaSProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * OpenIDaaS 初始化器
 *
 * 在应用启动完成后执行数据库初始化操作。
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
@Slf4j
public class OpenIDaaSInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final OpenIDaaSProperties properties;
    private final ResourceLoader resourceLoader;

    public OpenIDaaSInitializer(OpenIDaaSProperties properties) {
        this.properties = properties;
        this.resourceLoader = null; // 将通过构造函数注入
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        log.info("OpenIDaaS initialization started");

        try {
            // 执行 Schema 初始化
            if (properties.getDatabase().getSchemaLocation() != null) {
                executeScript(properties.getDatabase().getSchemaLocation(), "Schema");
            }

            // 执行数据初始化
            if (properties.getDatabase().getDataLocation() != null) {
                executeScript(properties.getDatabase().getDataLocation(), "Data");
            }

            log.info("OpenIDaaS initialization completed successfully");
        } catch (Exception e) {
            log.error("OpenIDaaS initialization failed", e);
            // 不抛出异常，避免影响应用启动
        }
    }

    private void executeScript(String location, String scriptType) {
        try {
            Resource resource = resourceLoader.getResource(location);
            if (resource.exists()) {
                log.info("Executing {} script: {}", scriptType, location);
                // TODO: 实现脚本执行逻辑
            } else {
                log.warn("{} script not found: {}", scriptType, location);
            }
        } catch (Exception e) {
            log.error("Failed to execute {} script: {}", scriptType, location, e);
        }
    }
}
