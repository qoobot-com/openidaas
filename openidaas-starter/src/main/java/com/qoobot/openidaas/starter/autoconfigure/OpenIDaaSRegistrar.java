package com.qoobot.openidaas.starter.autoconfigure;

import com.qoobot.openidaas.starter.EnableOpenIDaaS;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

/**
 * OpenIDaaS Bean 定义注册器
 *
 * 根据 @EnableOpenIDaaS 注解的属性，动态注册 Bean 定义。
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
public class OpenIDaaSRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                       BeanDefinitionRegistry registry) {
        // 注册 OpenIDaaS 自动配置类
        registerAutoConfiguration(registry, OpenIDaaSAutoConfiguration.class);

        // 如果存在 @EnableOpenIDaaS 注解，根据注解属性注册对应的配置类
        if (importingClassMetadata.isAnnotated(EnableOpenIDaaS.class.getName())) {
            // 读取注解属性
            boolean enableAuth = getAnnotationAttribute(importingClassMetadata,
                EnableOpenIDaaS.class, "enableAuth", true);
            boolean enableUser = getAnnotationAttribute(importingClassMetadata,
                EnableOpenIDaaS.class, "enableUser", true);
            boolean enableTenant = getAnnotationAttribute(importingClassMetadata,
                EnableOpenIDaaS.class, "enableTenant", true);
            boolean enableSecurity = getAnnotationAttribute(importingClassMetadata,
                EnableOpenIDaaS.class, "enableSecurity", true);
            boolean enableGateway = getAnnotationAttribute(importingClassMetadata,
                EnableOpenIDaaS.class, "enableGateway", false);
            boolean enableHealthCheck = getAnnotationAttribute(importingClassMetadata,
                EnableOpenIDaaS.class, "enableHealthCheck", true);

            // 根据属性注册对应的配置类
            if (enableAuth) {
                registerAutoConfiguration(registry, OpenIDaaSAuthAutoConfiguration.class);
            }
            if (enableUser) {
                registerAutoConfiguration(registry, OpenIDaaSUserAutoConfiguration.class);
            }
            if (enableTenant) {
                registerAutoConfiguration(registry, OpenIDaaSTenantAutoConfiguration.class);
            }
            if (enableSecurity) {
                registerAutoConfiguration(registry, OpenIDaaSSecurityAutoConfiguration.class);
            }
            if (enableGateway) {
                registerAutoConfiguration(registry, OpenIDaaSGatewayAutoConfiguration.class);
            }
            if (enableHealthCheck) {
                registerAutoConfiguration(registry,
                    "com.qoobot.openidaas.starter.health.OpenIDaaSHealthIndicator");
            }
        }
    }

    /**
     * 注册自动配置类
     */
    private void registerAutoConfiguration(BeanDefinitionRegistry registry, Class<?> configClass) {
        if (!registry.containsBeanDefinition(configClass.getName())) {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(configClass);
            beanDefinition.setRole(GenericBeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(configClass.getName(), beanDefinition);
        }
    }

    /**
     * 注册自动配置类（通过类名）
     */
    private void registerAutoConfiguration(BeanDefinitionRegistry registry, String className) {
        if (!registry.containsBeanDefinition(className)) {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setRole(GenericBeanDefinition.ROLE_INFRASTRUCTURE);
            registry.registerBeanDefinition(className, beanDefinition);
        }
    }

    /**
     * 获取注解属性
     */
    @SuppressWarnings("unchecked")
    private <T> T getAnnotationAttribute(AnnotationMetadata metadata,
                                       Class<?> annotationClass,
                                       String attributeName,
                                       T defaultValue) {
        try {
            return (T) metadata.getAnnotationAttributes(annotationClass.getName())
                .getOrDefault(attributeName, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
