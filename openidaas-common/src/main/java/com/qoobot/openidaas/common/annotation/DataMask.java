package com.qoobot.openidaas.common.annotation;

import java.lang.annotation.*;

/**
 * 数据脱敏注解
 * 用于在返回VO时对敏感数据进行脱敏处理
 *
 * @author QooBot
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DataMask {

    /**
     * 脱敏类型
     */
    MaskType value() default MaskType.CUSTOM;

    /**
     * 自定义脱敏规则
     * 例如: phone表示手机号,email表示邮箱
     */
    String pattern() default "";

    /**
     * 脱敏字符
     */
    String maskChar() default "*";

    /**
     * 脱敏类型枚举
     */
    enum MaskType {
        /**
         * 手机号脱敏: 138****8000
         */
        PHONE,

        /**
         * 邮箱脱敏: t***@example.com
         */
        EMAIL,

        /**
         * 身份证号脱敏: 110101********1234
         */
        ID_CARD,

        /**
         * 银行卡号脱敏: 6222****1234
         */
        BANK_CARD,

        /**
         * 密码: 完全隐藏
         */
        PASSWORD,

        /**
         * 地址: 北京市朝阳区****
         */
        ADDRESS,

        /**
         * 自定义脱敏
         */
        CUSTOM,

        /**
         * 名字: 张**
         */
        NAME
    }
}
