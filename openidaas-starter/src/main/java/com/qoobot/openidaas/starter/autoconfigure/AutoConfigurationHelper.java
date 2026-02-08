package com.qoobot.openidaas.starter.autoconfigure;

/**
 * 自动配置辅助工具类
 *
 * 提供类检测等辅助功能。
 *
 * @author OpenIDaaS Team
 * @since 1.0.0
 */
public final class AutoConfigurationHelper {

    private AutoConfigurationHelper() {
        // 工具类，不允许实例化
    }

    /**
     * 检查类是否存在
     *
     * @param className 类名
     * @return 如果类存在返回 true，否则返回 false
     */
    public static boolean isClassPresent(String className) {
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 检查多个类是否全部存在
     *
     * @param classNames 类名数组
     * @return 如果所有类都存在返回 true，否则返回 false
     */
    public static boolean areAllClassesPresent(String... classNames) {
        for (String className : classNames) {
            if (!isClassPresent(className)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查任意一个类是否存在
     *
     * @param classNames 类名数组
     * @return 如果任意一个类存在返回 true，否则返回 false
     */
    public static boolean isAnyClassPresent(String... classNames) {
        for (String className : classNames) {
            if (isClassPresent(className)) {
                return true;
            }
        }
        return false;
    }
}
