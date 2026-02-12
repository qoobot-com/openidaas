package com.qoobot.openidaas.common.util;

import com.qoobot.openidaas.common.annotation.DataMask;

/**
 * 数据脱敏工具类
 *
 * @author QooBot
 */
public class DataMaskUtil {

    /**
     * 对数据进行脱敏
     *
     * @param data 原始数据
     * @param maskType 脱敏类型
     * @return 脱敏后的数据
     */
    public static String mask(String data, DataMask.MaskType maskType) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        return switch (maskType) {
            case PHONE -> maskPhone(data);
            case EMAIL -> maskEmail(data);
            case ID_CARD -> maskIdCard(data);
            case BANK_CARD -> maskBankCard(data);
            case PASSWORD -> maskPassword();
            case ADDRESS -> maskAddress(data);
            case NAME -> maskName(data);
            case CUSTOM -> data; // 自定义脱敏需要根据具体规则实现
        };
    }

    /**
     * 手机号脱敏: 138****8000
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /**
     * 邮箱脱敏: t***@example.com
     */
    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        int atIndex = email.indexOf("@");
        String prefix = email.substring(0, atIndex);
        String suffix = email.substring(atIndex);

        if (prefix.length() <= 1) {
            return prefix + "***" + suffix;
        }

        String maskedPrefix = prefix.charAt(0) + "***";
        return maskedPrefix + suffix;
    }

    /**
     * 身份证号脱敏: 110101********1234
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 18) {
            return idCard;
        }
        return idCard.substring(0, 6) + "********" + idCard.substring(14);
    }

    /**
     * 银行卡号脱敏: 6222****1234
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.length() < 16) {
            return bankCard;
        }
        return bankCard.substring(0, 4) + "****" + bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 密码完全隐藏
     */
    public static String maskPassword() {
        return "******";
    }

    /**
     * 地址脱敏: 北京市朝阳区****
     */
    public static String maskAddress(String address) {
        if (address == null || address.length() < 6) {
            return address;
        }
        return address.substring(0, Math.min(6, address.length())) + "****";
    }

    /**
     * 名字脱敏: 张**
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return "*";
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "**";
    }

    /**
     * 自定义脱敏
     *
     * @param data 原始数据
     * @param keepStart 保留开头字符数
     * @param keepEnd 保留结尾字符数
     * @param maskChar 脱敏字符
     * @return 脱敏后的数据
     */
    public static String customMask(String data, int keepStart, int keepEnd, String maskChar) {
        if (data == null || data.isEmpty()) {
            return data;
        }

        if (data.length() <= keepStart + keepEnd) {
            return maskChar.repeat(data.length());
        }

        String start = data.substring(0, keepStart);
        String end = data.substring(data.length() - keepEnd);
        String middle = maskChar.repeat(data.length() - keepStart - keepEnd);

        return start + middle + end;
    }
}
