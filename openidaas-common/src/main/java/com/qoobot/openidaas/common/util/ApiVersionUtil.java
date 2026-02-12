package com.qoobot.openidaas.common.util;

import com.qoobot.openidaas.common.annotation.ApiVersion;
import com.qoobot.openidaas.common.annotation.DeprecatedApi;
import com.qoobot.openidaas.common.config.ApiVersionConfig;

import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

/**
 * API 版本工具类
 *
 * @author QooBot
 */
public class ApiVersionUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 从请求路径中提取版本号
     *
     * @param path 请求路径
     * @return 版本号
     */
    public static String extractVersionFromPath(String path) {
        if (path == null || path.isEmpty()) {
            return ApiVersionConfig.DEFAULT_VERSION;
        }

        String[] parts = path.split("/");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].startsWith("v") && parts[i].length() > 1) {
                String version = parts[i];
                if (ApiVersionConfig.isVersionSupported(version)) {
                    return version;
                }
            }
        }

        return ApiVersionConfig.DEFAULT_VERSION;
    }

    /**
     * 检查类是否支持指定版本
     *
     * @param clazz  类
     * @param version 版本号
     * @return 是否支持
     */
    public static boolean supportsVersion(Class<?> clazz, String version) {
        ApiVersion annotation = clazz.getAnnotation(ApiVersion.class);
        if (annotation == null) {
            // 默认支持 v1
            return ApiVersionConfig.DEFAULT_VERSION.equals(version);
        }
        return annotation.value().equals(version);
    }

    /**
     * 检查方法是否支持指定版本
     *
     * @param method  方法
     * @param version 版本号
     * @return 是否支持
     */
    public static boolean supportsVersion(Method method, String version) {
        // 检查方法上的注解
        ApiVersion annotation = method.getAnnotation(ApiVersion.class);
        if (annotation != null) {
            return annotation.value().equals(version);
        }

        // 检查类上的注解
        return supportsVersion(method.getDeclaringClass(), version);
    }

    /**
     * 检查类是否已废弃
     *
     * @param clazz 类
     * @return 是否已废弃
     */
    public static boolean isDeprecated(Class<?> clazz) {
        ApiVersion apiVersion = clazz.getAnnotation(ApiVersion.class);
        if (apiVersion != null && apiVersion.deprecated()) {
            return true;
        }

        DeprecatedApi deprecatedApi = clazz.getAnnotation(DeprecatedApi.class);
        return deprecatedApi != null;
    }

    /**
     * 检查方法是否已废弃
     *
     * @param method 方法
     * @return 是否已废弃
     */
    public static boolean isDeprecated(Method method) {
        ApiVersion apiVersion = method.getAnnotation(ApiVersion.class);
        if (apiVersion != null && apiVersion.deprecated()) {
            return true;
        }

        DeprecatedApi deprecatedApi = method.getAnnotation(DeprecatedApi.class);
        return deprecatedApi != null;
    }

    /**
     * 获取废弃日期
     *
     * @param clazz 类
     * @return 废弃日期
     */
    public static LocalDate getDeprecationDate(Class<?> clazz) {
        ApiVersion apiVersion = clazz.getAnnotation(ApiVersion.class);
        if (apiVersion != null && apiVersion.deprecated() && !apiVersion.deprecationDate().isEmpty()) {
            return LocalDate.parse(apiVersion.deprecationDate(), DATE_FORMATTER);
        }

        DeprecatedApi deprecatedApi = clazz.getAnnotation(DeprecatedApi.class);
        if (deprecatedApi != null && !deprecatedApi.value().isEmpty()) {
            return LocalDate.parse(deprecatedApi.value(), DATE_FORMATTER);
        }

        return null;
    }

    /**
     * 获取推荐版本
     *
     * @param clazz 类
     * @return 推荐版本
     */
    public static String getRecommendedVersion(Class<?> clazz) {
        ApiVersion apiVersion = clazz.getAnnotation(ApiVersion.class);
        if (apiVersion != null && !apiVersion.recommendedVersion().isEmpty()) {
            return apiVersion.recommendedVersion();
        }

        DeprecatedApi deprecatedApi = clazz.getAnnotation(DeprecatedApi.class);
        if (deprecatedApi != null && !deprecatedApi.replacement().isEmpty()) {
            return extractVersionFromPath(deprecatedApi.replacement());
        }

        return null;
    }

    /**
     * 构建版本化路径
     *
     * @param version 版本号
     * @param path    原始路径
     * @return 版本化路径
     */
    public static String buildVersionedPath(String version, String path) {
        return ApiVersionConfig.buildVersionedPath(version, path);
    }

    /**
     * 获取所有支持的版本
     *
     * @return 版本列表
     */
    public static List<String> getSupportedVersions() {
        return Arrays.asList(ApiVersionConfig.SUPPORTED_VERSIONS);
    }

    /**
     * 检查是否为最新版本
     *
     * @param version 版本号
     * @return 是否为最新版本
     */
    public static boolean isLatestVersion(String version) {
        String[] versions = ApiVersionConfig.SUPPORTED_VERSIONS;
        return versions.length > 0 && versions[versions.length - 1].equals(version);
    }

    /**
     * 获取下一个版本
     *
     * @param currentVersion 当前版本
     * @return 下一个版本
     */
    public static String getNextVersion(String currentVersion) {
        String[] versions = ApiVersionConfig.SUPPORTED_VERSIONS;
        for (int i = 0; i < versions.length - 1; i++) {
            if (versions[i].equals(currentVersion)) {
                return versions[i + 1];
            }
        }
        return null;
    }
}
