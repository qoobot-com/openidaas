package com.qoobot.openidaas.common.util;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Pattern;

/**
 * 字符串工具类
 *
 * @author QooBot
 */
@Slf4j
public class StringUtil {

    /**
     * 手机号正则表达式
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    /**
     * 身份证号正则表达式
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");

    /**
     * 判断字符串是否为空
     */
    public static boolean isEmpty(String str) {
        return StrUtil.isEmpty(str);
    }

    /**
     * 判断字符串是否非空
     */
    public static boolean isNotEmpty(String str) {
        return StrUtil.isNotEmpty(str);
    }

    /**
     * 判断字符串是否为空白
     */
    public static boolean isBlank(String str) {
        return StrUtil.isBlank(str);
    }

    /**
     * 判断字符串是否非空白
     */
    public static boolean isNotBlank(String str) {
        return StrUtil.isNotBlank(str);
    }

    /**
     * 去除字符串首尾空白字符
     */
    public static String trim(String str) {
        return StrUtil.trim(str);
    }

    /**
     * 字符串连接
     */
    public static String join(CharSequence delimiter, CharSequence... elements) {
        return StrUtil.join(delimiter, (Object[]) elements);
    }

    /**
     * 字符串格式化
     */
    public static String format(String template, Object... params) {
        return StrUtil.format(template, params);
    }

    /**
     * 判断是否为手机号
     */
    public static boolean isPhone(String phone) {
        return isNotEmpty(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 判断是否为邮箱
     */
    public static boolean isEmail(String email) {
        return isNotEmpty(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 判断是否为身份证号
     */
    public static boolean isIdCard(String idCard) {
        return isNotEmpty(idCard) && ID_CARD_PATTERN.matcher(idCard).matches();
    }

    /**
     * 隐藏手机号中间四位
     */
    public static String hidePhone(String phone) {
        if (!isPhone(phone)) {
            return phone;
        }
        return phone.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
    }

    /**
     * 隐藏邮箱用户名部分
     */
    public static String hideEmail(String email) {
        if (!isEmail(email)) {
            return email;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 1) {
            return email;
        }
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        if (username.length() <= 2) {
            return "*".repeat(username.length()) + domain;
        }
        return username.charAt(0) + "***" + username.charAt(username.length() - 1) + domain;
    }

    /**
     * 隐藏身份证号中间部分
     */
    public static String hideIdCard(String idCard) {
        if (!isIdCard(idCard)) {
            return idCard;
        }
        return idCard.replaceAll("(\\d{6})\\d{8}(\\d{4})", "$1********$2");
    }

    /**
     * 下划线转驼峰
     */
    public static String toCamelCase(String str) {
        return StrUtil.toCamelCase(str);
    }

    /**
     * 驼峰转下划线
     */
    public static String toUnderlineCase(String str) {
        return StrUtil.toUnderlineCase(str);
    }

    /**
     * 首字母大写
     */
    public static String capitalize(String str) {
        return StrUtil.upperFirst(str);
    }

    /**
     * 首字母小写
     */
    public static String uncapitalize(String str) {
        return StrUtil.lowerFirst(str);
    }

    /**
     * 字符串截取（安全）
     */
    public static String substring(String str, int start, int end) {
        if (isEmpty(str)) {
            return str;
        }
        if (start < 0) {
            start = 0;
        }
        if (end > str.length()) {
            end = str.length();
        }
        if (start >= end) {
            return "";
        }
        return str.substring(start, end);
    }

    /**
     * 字符串左侧填充
     */
    public static String padStart(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }
        if (str.length() >= length) {
            return str;
        }
        StringBuilder sb = new StringBuilder(length);
        for (int i = str.length(); i < length; i++) {
            sb.append(padChar);
        }
        sb.append(str);
        return sb.toString();
    }

    /**
     * 字符串右侧填充
     */
    public static String padEnd(String str, int length, char padChar) {
        if (str == null) {
            str = "";
        }
        if (str.length() >= length) {
            return str;
        }
        StringBuilder sb = new StringBuilder(str);
        while (sb.length() < length) {
            sb.append(padChar);
        }
        return sb.toString();
    }

    /**
     * 判断字符串是否包含中文
     */
    public static boolean containsChinese(String str) {
        if (isEmpty(str)) {
            return false;
        }
        return str.matches(".*[\u4e00-\u9fa5]+.*");
    }

    /**
     * 判断字符串是否只包含数字
     */
    public static boolean isNumeric(String str) {
        return isNotEmpty(str) && str.matches("\\d+");
    }

    /**
     * 判断字符串是否只包含字母
     */
    public static boolean isAlpha(String str) {
        return isNotEmpty(str) && str.matches("[a-zA-Z]+");
    }

    /**
     * 判断字符串是否只包含字母和数字
     */
    public static boolean isAlphanumeric(String str) {
        return isNotEmpty(str) && str.matches("[a-zA-Z0-9]+");
    }

    /**
     * 生成指定长度的随机字符串
     */
    public static String randomString(int length) {
        return cn.hutool.core.util.RandomUtil.randomString(length);
    }

    /**
     * 生成指定长度的数字字符串
     */
    public static String randomNumber(int length) {
        return cn.hutool.core.util.RandomUtil.randomNumbers(length);
    }

    /**
     * 比较两个字符串是否相等（忽略大小写）
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        return StrUtil.equalsIgnoreCase(str1, str2);
    }

    /**
     * 判断字符串是否以指定前缀开头
     */
    public static boolean startsWith(String str, String prefix) {
        return StrUtil.startWith(str, prefix);
    }

    /**
     * 判断字符串是否以指定后缀结尾
     */
    public static boolean endsWith(String str, String suffix) {
        return StrUtil.endWith(str, suffix);
    }

    public static String generateRandomString(int i) {
        return RandomUtil.randomString(i);
    }
}