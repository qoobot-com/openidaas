package com.qoobot.openidaas.common.config;

import org.springframework.context.annotation.Configuration;

/**
 * API 版本管理配置
 *
 * @author QooBot
 */
@Configuration
public class ApiVersionConfig {

    /**
     * 当前支持的 API 版本
     */
    public static final String[] SUPPORTED_VERSIONS = {"v1", "v2"};

    /**
     * 默认 API 版本
     */
    public static final String DEFAULT_VERSION = "v1";

    /**
     * API 版本路径前缀
     */
    public static final String API_PATH_PREFIX = "/api";

    /**
     * 检查版本是否受支持
     *
     * @param version 版本号
     * @return 是否支持
     */
    public static boolean isVersionSupported(String version) {
        if (version == null || version.isEmpty()) {
            return false;
        }
        for (String supportedVersion : SUPPORTED_VERSIONS) {
            if (supportedVersion.equals(version)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取默认版本
     *
     * @return 默认版本号
     */
    public static String getDefaultVersion() {
        return DEFAULT_VERSION;
    }

    /**
     * 构建版本化 API 路径
     *
     * @param version 版本号
     * @param path    路径
     * @return 版本化路径
     */
    public static String buildVersionedPath(String version, String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        // 如果路径已包含版本，直接返回
        if (path.startsWith(API_PATH_PREFIX + "/v")) {
            return path;
        }
        // 如果路径以 /api 开头，替换为 /api/vX/
        if (path.startsWith(API_PATH_PREFIX + "/")) {
            String resourcePath = path.substring(API_PATH_PREFIX.length() + 1);
            return API_PATH_PREFIX + "/" + version + "/" + resourcePath;
        }
        // 否则添加版本前缀
        return API_PATH_PREFIX + "/" + version + "/" + path;
    }
}
