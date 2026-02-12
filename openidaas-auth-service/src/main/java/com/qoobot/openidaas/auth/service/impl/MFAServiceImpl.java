package com.qoobot.openidaas.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qoobot.openidaas.auth.enumeration.MFAStatus;
import com.qoobot.openidaas.auth.enumeration.MFAType;
import com.qoobot.openidaas.auth.entity.MFABackupCode;
import com.qoobot.openidaas.auth.entity.MFALog;
import com.qoobot.openidaas.auth.entity.UserMFAFactor;
import com.qoobot.openidaas.auth.mapper.MFABackupCodeMapper;
import com.qoobot.openidaas.auth.mapper.MFALogMapper;
import com.qoobot.openidaas.auth.mapper.UserMFAFactorMapper;
import com.qoobot.openidaas.auth.service.MFAService;
import com.qoobot.openidaas.auth.util.TOTPUtil;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.auth.util.AesUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * MFA服务实现类
 *
 * @author QooBot
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MFAServiceImpl implements MFAService {

    private final UserMFAFactorMapper mfaFactorMapper;
    private final MFABackupCodeMapper backupCodeMapper;
    private final MFALogMapper mfaLogMapper;
    private final StringRedisTemplate redisTemplate;

    private static final String SMS_CODE_PREFIX = "mfa:sms:";
    private static final String EMAIL_CODE_PREFIX = "mfa:email:";
    private static final int SMS_CODE_EXPIRE_SECONDS = 300; // 5分钟
    private static final int EMAIL_CODE_EXPIRE_SECONDS = 300; // 5分钟
    private static final int CODE_LENGTH = 6;
    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final int LOCK_DURATION_MINUTES = 30;

    @Override
    public Map<String, Object> generateTOTPSetup(Long userId, String issuer) {
        log.info("生成TOTP设置信息，userId: {}", userId);

        // 生成密钥
        String secret = TOTPUtil.generateSecret();

        // 生成otpauth URI
        String otpAuthURI = TOTPUtil.generateOtpAuthURI(secret, "user:" + userId, issuer);

        // 创建MFA因子（待验证状态）
        UserMFAFactor factor = new UserMFAFactor();
        factor.setUserId(userId);
        factor.setMFAType(MFAType.TOTP);
        factor.setFactorName("Google Authenticator");
        factor.setSecret(AesUtil.encrypt(secret)); // 加密存储
        factor.setIsPrimary(0);
        factor.setStatus(MFAStatus.PENDING.getCode());
        factor.setFailedAttempts(0);
        factor.setVerificationCount(0L);
        mfaFactorMapper.insert(factor);

        Map<String, Object> result = new HashMap<>();
        result.put("factorId", factor.getId());
        result.put("secret", secret);
        result.put("otpAuthURI", otpAuthURI);
        result.put("qrCode", generateQRCode(otpAuthURI));
        result.put("remainingSeconds", TOTPUtil.getRemainingSeconds());

        log.info("TOTP设置信息生成成功，factorId: {}", factor.getId());
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyAndActivateTOTP(Long userId, String secret, String code) {
        log.info("验证并激活TOTP，userId: {}", userId);

        // 查询待验证的TOTP因子
        LambdaQueryWrapper<UserMFAFactor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMFAFactor::getUserId, userId)
                .eq(UserMFAFactor::getFactorType, MFAType.TOTP.getCode())
                .eq(UserMFAFactor::getStatus, MFAStatus.PENDING.getCode())
                .orderByDesc(UserMFAFactor::getCreatedAt)
                .last("LIMIT 1");
        UserMFAFactor factor = mfaFactorMapper.selectOne(wrapper);

        if (factor == null) {
            throw new BusinessException("未找到待验证的MFA因子");
        }

        // 验证代码
        if (!TOTPUtil.verify(secret, code)) {
            factor.setFailedAttempts(factor.getFailedAttempts() + 1);
            mfaFactorMapper.updateById(factor);
            throw new BusinessException("验证码错误");
        }

        // 验证加密后的secret是否匹配
        String encryptedSecret = AesUtil.encrypt(secret);
        if (!encryptedSecret.equals(factor.getSecret())) {
            throw new BusinessException("密钥不匹配");
        }

        // 激活MFA
        factor.setStatus(MFAStatus.ACTIVE.getCode());

        // 如果是第一个激活的MFA，设为主MFA
        int count = mfaFactorMapper.countActiveByUserId(userId);
        if (count == 0) {
            factor.setIsPrimary(1);
        } else {
            factor.setIsPrimary(0);
        }

        mfaFactorMapper.updateById(factor);

        // 生成备用码
        generateBackupCodes(userId, factor.getId(), 10);

        log.info("TOTP激活成功，factorId: {}", factor.getId());
        return true;
    }

    @Override
    public boolean verifyTOTP(Long userId, String code) {
        log.info("验证TOTP，userId: {}", userId);

        // 查询激活的TOTP因子
        UserMFAFactor factor = mfaFactorMapper.selectPrimaryByUserId(userId);
        if (factor == null || !MFAType.TOTP.getCode().equals(factor.getFactorType())) {
            throw new BusinessException("未配置TOTP");
        }

        // 检查是否锁定
        if (factor.isLocked()) {
            throw new BusinessException("MFA已锁定，请稍后再试");
        }

        // 解密密钥
        String secret = AesUtil.decrypt(factor.getSecret());

        // 验证代码
        if (!TOTPUtil.verify(secret, code)) {
            factor.setFailedAttempts(factor.getFailedAttempts() + 1);

            // 超过最大失败次数则锁定
            if (factor.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                factor.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_DURATION_MINUTES));
                mfaFactorMapper.updateById(factor);
                throw new BusinessException("验证失败次数过多，MFA已锁定" + LOCK_DURATION_MINUTES + "分钟");
            }

            mfaFactorMapper.updateById(factor);
            return false;
        }

        // 验证成功，重置失败次数
        factor.setFailedAttempts(0);
        factor.setLastUsedAt(LocalDateTime.now());
        factor.setVerificationCount(factor.getVerificationCount() + 1);
        mfaFactorMapper.updateById(factor);

        return true;
    }

    @Override
    public boolean sendSMSCode(Long userId, String phoneNumber) {
        log.info("发送短信验证码，userId: {}", userId);

        // 生成验证码
        String code = generateRandomCode(CODE_LENGTH);
        String redisKey = SMS_CODE_PREFIX + userId;

        // 存储到Redis（5分钟过期）
        redisTemplate.opsForValue().set(redisKey, code, SMS_CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // TODO: 调用短信服务发送验证码
        log.info("短信验证码已生成并存储，phoneNumber: {}, code: {}", phoneNumber, code);

        return true;
    }

    @Override
    public boolean verifySMSCode(Long userId, String code) {
        log.info("验证短信验证码，userId: {}", userId);

        String redisKey = SMS_CODE_PREFIX + userId;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            throw new BusinessException("验证码已过期或不存在");
        }

        if (!storedCode.equals(code)) {
            throw new BusinessException("验证码错误");
        }

        // 验证成功，删除验证码
        redisTemplate.delete(redisKey);

        return true;
    }

    @Override
    public boolean sendEmailCode(Long userId, String email) {
        log.info("发送邮箱验证码，userId: {}", userId);

        // 生成验证码
        String code = generateRandomCode(CODE_LENGTH);
        String redisKey = EMAIL_CODE_PREFIX + userId;

        // 存储到Redis（5分钟过期）
        redisTemplate.opsForValue().set(redisKey, code, EMAIL_CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);

        // TODO: 调用邮件服务发送验证码
        log.info("邮箱验证码已生成并存储，email: {}, code: {}", email, code);

        return true;
    }

    @Override
    public boolean verifyEmailCode(Long userId, String code) {
        log.info("验证邮箱验证码，userId: {}", userId);

        String redisKey = EMAIL_CODE_PREFIX + userId;
        String storedCode = redisTemplate.opsForValue().get(redisKey);

        if (storedCode == null) {
            throw new BusinessException("验证码已过期或不存在");
        }

        if (!storedCode.equals(code)) {
            throw new BusinessException("验证码错误");
        }

        // 验证成功，删除验证码
        redisTemplate.delete(redisKey);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> generateBackupCodes(Long userId, int count) {
        log.info("生成备用码，userId: {}, count: {}", userId, count);

        // 获取主MFA因子
        UserMFAFactor factor = mfaFactorMapper.selectPrimaryByUserId(userId);
        if (factor == null) {
            throw new BusinessException("未配置MFA");
        }

        return generateBackupCodes(userId, factor.getId(), count);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean verifyBackupCode(Long userId, String code) {
        log.info("验证备用码，userId: {}", userId);

        // 查询用户的备用码
        LambdaQueryWrapper<UserMFAFactor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMFAFactor::getUserId, userId)
                .eq(UserMFAFactor::getFactorType, MFAType.BACKUP_CODE.getCode())
                .eq(UserMFAFactor::getStatus, MFAStatus.ACTIVE.getCode());
        UserMFAFactor factor = mfaFactorMapper.selectOne(wrapper);

        if (factor == null) {
            throw new BusinessException("未找到备用码配置");
        }

        // 查询未使用的备用码
        List<MFABackupCode> backupCodes = backupCodeMapper.selectUnusedByUserIdAndFactorId(userId, factor.getId());
        if (backupCodes.isEmpty()) {
            throw new BusinessException("备用码已用完，请重新生成");
        }

        // 验证备用码
        for (MFABackupCode backupCode : backupCodes) {
            if (backupCode.getCodeHash().equals(hashCode(code))) {
                // 标记为已使用
                backupCode.setIsUsed(1);
                backupCode.setUsedAt(LocalDateTime.now());
                backupCodeMapper.updateById(backupCode);

                // 检查剩余备用码数量，如果少于3个则提示
                int remaining = backupCodeMapper.countUnusedByUserIdAndFactorId(userId, factor.getId());
                if (remaining < 3) {
                    log.warn("备用码剩余数量不足，userId: {}, remaining: {}", userId, remaining);
                }

                return true;
            }
        }

        throw new BusinessException("备用码错误");
    }

    @Override
    public Map<String, Object> getUserMFAPreferences(Long userId) {
        log.info("获取用户MFA偏好设置，userId: {}", userId);

        List<UserMFAFactor> factors = mfaFactorMapper.selectByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("mfaEnabled", !factors.isEmpty());
        result.put("factors", factors.stream()
                .filter(f -> f.getStatus() == null || f.getStatus() != MFAStatus.DELETED.getCode())
                .map(this::convertFactorToMap)
                .collect(Collectors.toList()));

        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableMFAPreference(Long userId, Long factorId) {
        log.info("禁用MFA因子，userId: {}, factorId: {}", userId, factorId);

        UserMFAFactor factor = mfaFactorMapper.selectById(factorId);
        if (factor == null || !factor.getUserId().equals(userId)) {
            throw new BusinessException("MFA因子不存在");
        }

        factor.setStatus(MFAStatus.DISABLED.getCode());
        mfaFactorMapper.updateById(factor);

        // 如果禁用的是主MFA，且还有其他激活的MFA，则设置第一个为主MFA
        if (factor.isPrimary()) {
            LambdaQueryWrapper<UserMFAFactor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserMFAFactor::getUserId, userId)
                    .eq(UserMFAFactor::getStatus, MFAStatus.ACTIVE.getCode())
                    .ne(UserMFAFactor::getId, factorId)
                    .orderByAsc(UserMFAFactor::getCreatedAt)
                    .last("LIMIT 1");
            UserMFAFactor newPrimary = mfaFactorMapper.selectOne(wrapper);
            if (newPrimary != null) {
                newPrimary.setIsPrimary(1);
                mfaFactorMapper.updateById(newPrimary);
            }
        }

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setPrimaryMFA(Long userId, Long factorId) {
        log.info("设置主MFA，userId: {}, factorId: {}", userId, factorId);

        UserMFAFactor factor = mfaFactorMapper.selectById(factorId);
        if (factor == null || !factor.getUserId().equals(userId)) {
            throw new BusinessException("MFA因子不存在");
        }

        if (!factor.isActive()) {
            throw new BusinessException("只能设置激活的MFA为主MFA");
        }

        // 取消所有其他MFA的主标记
        LambdaQueryWrapper<UserMFAFactor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserMFAFactor::getUserId, userId)
                .eq(UserMFAFactor::getIsPrimary, 1);
        List<UserMFAFactor> primaryFactors = mfaFactorMapper.selectList(wrapper);

        for (UserMFAFactor f : primaryFactors) {
            f.setIsPrimary(0);
            mfaFactorMapper.updateById(f);
        }

        // 设置新的主MFA
        factor.setIsPrimary(1);
        mfaFactorMapper.updateById(factor);

        return true;
    }

    @Override
    public boolean isMFAEnabled(Long userId) {
        int count = mfaFactorMapper.countActiveByUserId(userId);
        return count > 0;
    }

    @Override
    public boolean verifyMFA(Long userId, String mfaCode, String clientIp) {
        log.info("验证MFA，userId: {}", userId);

        // 检查是否启用了MFA
        if (!isMFAEnabled(userId)) {
            return true; // 未启用MFA，直接通过
        }

        // 记录验证日志
        MFALog mfaLog = new MFALog();
        mfaLog.setUserId(userId);
        mfaLog.setClientIp(clientIp);

        try {
            // 先尝试主MFA
            UserMFAFactor primaryFactor = mfaFactorMapper.selectPrimaryByUserId(userId);
            if (primaryFactor == null) {
                throw new BusinessException("未找到主MFA配置");
            }

            mfaLog.setFactorId(primaryFactor.getId());
            mfaLog.setFactorType(primaryFactor.getFactorType());

            boolean result;
            MFAType mfaType = primaryFactor.getMFAType();

            switch (mfaType) {
                case TOTP:
                    result = verifyTOTP(userId, mfaCode);
                    break;
                case SMS:
                    result = verifySMSCode(userId, mfaCode);
                    break;
                case EMAIL:
                    result = verifyEmailCode(userId, mfaCode);
                    break;
                case BACKUP_CODE:
                    result = verifyBackupCode(userId, mfaCode);
                    break;
                default:
                    throw new BusinessException("不支持的MFA类型: " + mfaType);
            }

            mfaLog.setResult(result ? "SUCCESS" : "FAILURE");
            mfaLog.setVerifiedAt(LocalDateTime.now());
            mfaLogMapper.insert(mfaLog);

            return result;

        } catch (Exception e) {
            mfaLog.setResult("FAILURE");
            mfaLog.setFailureReason(e.getMessage());
            mfaLog.setVerifiedAt(LocalDateTime.now());
            mfaLogMapper.insert(mfaLog);
            throw e;
        }
    }

    /**
     * 生成备用码
     */
    private Map<String, Object> generateBackupCodes(Long userId, Long factorId, int count) {
        // 删除旧备用码
        backupCodeMapper.deleteByUserIdAndFactorId(userId, factorId);

        // 创建备用码因子
        UserMFAFactor backupFactor = new UserMFAFactor();
        backupFactor.setUserId(userId);
        backupFactor.setMFAType(MFAType.BACKUP_CODE);
        backupFactor.setFactorName("备用码");
        backupFactor.setIsPrimary(0);
        backupFactor.setStatus(MFAStatus.ACTIVE.getCode());
        backupFactor.setFailedAttempts(0);
        backupFactor.setVerificationCount(0L);
        mfaFactorMapper.insert(backupFactor);

        // 生成备用码
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            String code = generateRandomCode(8); // 8位备用码
            codes.add(formatBackupCode(i + 1, count, code));

            // 存储备用码哈希
            MFABackupCode backupCode = new MFABackupCode();
            backupCode.setUserId(userId);
            backupCode.setFactorId(backupFactor.getId());
            backupCode.setCodeHash(hashCode(code));
            backupCode.setIsUsed(0);
            backupCode.setCreatedAt(LocalDateTime.now());
            backupCodeMapper.insert(backupCode);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("factorId", backupFactor.getId());
        result.put("codes", codes);
        result.put("count", count);

        log.info("备用码生成成功，factorId: {}, count: {}", backupFactor.getId(), count);
        return result;
    }

    /**
     * 格式化备用码
     */
    private String formatBackupCode(int index, int total, String code) {
        // 格式化：XXXX-XXXX-XXXX-XXXX
        return String.format("%04d-%04d-%04d-%04d",
                index, Integer.parseInt(code.substring(0, 2)),
                Integer.parseInt(code.substring(2, 4)), Integer.parseInt(code.substring(4)));
    }

    /**
     * 哈希备用码
     */
    private String hashCode(String code) {
        // 简单的哈希（实际应使用更强的哈希算法）
        return Integer.toHexString(code.hashCode());
    }

    /**
     * 生成随机验证码
     */
    private String generateRandomCode(int length) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成QR码
     */
    private String generateQRCode(String otpAuthURI) {
        // TODO: 使用QR码生成库生成二维码图片
        // 这里返回占位符
        return "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...";
    }

    /**
     * 转换因子为Map
     */
    private Map<String, Object> convertFactorToMap(UserMFAFactor factor) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", factor.getId());
        map.put("factorType", factor.getFactorType());
        map.put("factorName", factor.getFactorName());
        map.put("isPrimary", factor.isPrimary());
        map.put("status", factor.getStatus());
        map.put("statusText", factor.getStatus() != null ? MFAStatus.fromCode(factor.getStatus()).getDescription() : "未知");
        map.put("lastUsedAt", factor.getLastUsedAt());
        map.put("verificationCount", factor.getVerificationCount());
        return map;
    }
}
