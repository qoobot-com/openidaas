package com.qoobot.openidaas.auth.service;

import com.qoobot.openidaas.auth.enumeration.MFAType;

import java.util.Map;

/**
 * MFA服务接口
 *
 * @author QooBot
 */
public interface MFAService {

    /**
     * 生成MFA设置信息（TOTP）
     *
     * @param userId 用户ID
     * @param issuer 颁发者名称
     * @return 包含secret、qrCode等信息的Map
     */
    Map<String, Object> generateTOTPSetup(Long userId, String issuer);

    /**
     * 验证并激活TOTP
     *
     * @param userId 用户ID
     * @param secret 密钥
     * @param code 验证码
     * @return 是否成功
     */
    boolean verifyAndActivateTOTP(Long userId, String secret, String code);

    /**
     * 验证TOTP代码
     *
     * @param userId 用户ID
     * @param code 验证码
     * @return 是否成功
     */
    boolean verifyTOTP(Long userId, String code);

    /**
     * 发送短信验证码
     *
     * @param userId 用户ID
     * @param phoneNumber 手机号
     * @return 是否成功
     */
    boolean sendSMSCode(Long userId, String phoneNumber);

    /**
     * 验证短信验证码
     *
     * @param userId 用户ID
     * @param code 验证码
     * @return 是否成功
     */
    boolean verifySMSCode(Long userId, String code);

    /**
     * 发送邮箱验证码
     *
     * @param userId 用户ID
     * @param email 邮箱地址
     * @return 是否成功
     */
    boolean sendEmailCode(Long userId, String email);

    /**
     * 验证邮箱验证码
     *
     * @param userId 用户ID
     * @param code 验证码
     * @return 是否成功
     */
    boolean verifyEmailCode(Long userId, String code);

    /**
     * 生成备用码
     *
     * @param userId 用户ID
     * @param count 数量
     * @return 备用码列表
     */
    Map<String, Object> generateBackupCodes(Long userId, int count);

    /**
     * 验证备用码
     *
     * @param userId 用户ID
     * @param code 备用码
     * @return 是否成功
     */
    boolean verifyBackupCode(Long userId, String code);

    /**
     * 获取用户的MFA因子列表
     *
     * @param userId 用户ID
     * @return MFA因子列表
     */
    Map<String, Object> getUserMFAPreferences(Long userId);

    /**
     * 禁用MFA因子
     *
     * @param userId 用户ID
     * @param factorId 因子ID
     * @return 是否成功
     */
    boolean disableMFAPreference(Long userId, Long factorId);

    /**
     * 设置主MFA方式
     *
     * @param userId 用户ID
     * @param factorId 因子ID
     * @return 是否成功
     */
    boolean setPrimaryMFA(Long userId, Long factorId);

    /**
     * 检查用户是否启用了MFA
     *
     * @param userId 用户ID
     * @return 是否启用
     */
    boolean isMFAEnabled(Long userId);

    /**
     * 验证MFA代码（通用方法）
     *
     * @param userId 用户ID
     * @param mfaCode MFA代码
     * @param clientIp 客户端IP
     * @return 是否成功
     */
    boolean verifyMFA(Long userId, String mfaCode, String clientIp);
}
