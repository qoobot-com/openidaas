package com.qoobot.openidaas.auth.provider;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 多因子认证（MFA）提供者
 * 
 * 支持 TOTP（Time-based One-Time Password）
 * 基于 Google Authenticator 协议
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MfaAuthenticationProvider implements AuthenticationProvider {

    private final UserDetailsService userDetailsService;
    private final GoogleAuthenticator googleAuthenticator = new GoogleAuthenticator();
    
    // 存储临时生成的 MFA secret（5分钟有效期）
    private final ConcurrentHashMap<String, TempSecret> tempSecrets = new ConcurrentHashMap<>();

    /**
     * 为用户生成 MFA Secret
     * 
     * @param username 用户名
     * @return 包含 secret 和 QR Code URL 的对象
     */
    public MfaSecretInfo generateMfaSecret(String username) {
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        // 生成 TOTP secret
        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials(username);
        
        // 生成 QR Code URL
        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                username,
                "OpenIDaaS",
                key);
        
        // 存储临时 secret（5分钟有效期）
        String secretId = generateSecretId(username);
        tempSecrets.put(secretId, 
                new TempSecret(key.getKey(), System.currentTimeMillis()));
        
        log.info("Generated MFA secret for user: {}", username);
        
        return MfaSecretInfo.builder()
                .secretId(secretId)
                .secret(key.getKey())
                .qrCodeUrl(qrCodeUrl)
                .verificationCode(generateVerificationCode(key.getKey()))
                .expiresAt(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5))
                .build();
    }

    /**
     * 验证 MFA Code
     * 
     * @param username 用户名
     * @param code MFA 验证码
     * @return 是否验证成功
     */
    public boolean verifyMfaCode(String username, String code) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            
            // 从用户属性中获取 MFA secret
            String secret = (String) userDetails.getAuthorities().stream()
                    .findFirst()
                    .map(Object::toString)
                    .orElse(null);
            
            if (secret == null) {
                log.warn("No MFA secret found for user: {}", username);
                return false;
            }
            
            int verificationCode = Integer.parseInt(code);
            boolean isValid = googleAuthenticator.authorize(secret, verificationCode);
            
            if (isValid) {
                log.info("MFA verification successful for user: {}", username);
            } else {
                log.warn("Invalid MFA code for user: {}", username);
            }
            
            return isValid;
            
        } catch (NumberFormatException e) {
            log.error("Invalid MFA code format for user: {}", username, e);
            return false;
        }
    }

    /**
     * 确认并激活 MFA
     * 
     * @param username 用户名
     * @param secretId 临时 secret ID
     * @param code 验证码
     * @return 是否成功
     */
    public boolean confirmMfa(String username, String secretId, String code) {
        
        TempSecret tempSecret = tempSecrets.get(secretId);
        
        if (tempSecret == null) {
            log.warn("Invalid MFA secret ID for user: {}", username);
            return false;
        }
        
        // 检查是否过期
        if (System.currentTimeMillis() > tempSecret.expiresAt) {
            log.warn("MFA secret expired for user: {}", username);
            tempSecrets.remove(secretId);
            return false;
        }
        
        // 验证 code
        int verificationCode = Integer.parseInt(code);
        boolean isValid = googleAuthenticator.authorize(tempSecret.secret, verificationCode);
        
        if (isValid) {
            // 移除临时 secret
            tempSecrets.remove(secretId);
            log.info("MFA confirmed and activated for user: {}", username);
            
            // TODO: 将 secret 永久保存到数据库
            // saveMfaSecretToDatabase(username, tempSecret.secret);
        }
        
        return isValid;
    }

    /**
     * 禁用 MFA
     * 
     * @param username 用户名
     */
    public void disableMfa(String username) {
        // TODO: 从数据库中删除 MFA secret
        // removeMfaSecretFromDatabase(username);
        log.info("MFA disabled for user: {}", username);
    }

    /**
     * 生成备用验证码（用于离线场景）
     * 
     * @param username 用户名
     * @return 备用码列表
     */
    public BackupCodes generateBackupCodes(String username) {
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        String secret = (String) userDetails.getAuthorities().stream()
                .findFirst()
                .map(Object::toString)
                .orElse(null);
        
        if (secret == null) {
            throw new RuntimeException("MFA not enabled for user: " + username);
        }
        
        // 生成 10 个备用码
        java.util.List<String> codes = new java.util.ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String code = generateBackupCode();
            codes.add(code);
        }
        
        // TODO: 将备用码哈希后保存到数据库
        // saveBackupCodesToDatabase(username, codes);
        
        log.info("Generated backup codes for user: {}", username);
        
        return BackupCodes.builder()
                .codes(codes)
                .expiresAt(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(30))
                .build();
    }

    /**
     * 验证备用码
     * 
     * @param username 用户名
     * @param code 备用码
     * @return 是否有效
     */
    public boolean verifyBackupCode(String username, String code) {
        // TODO: 从数据库中查找并验证备用码
        // verifyBackupCodeInDatabase(username, code);
        return false;
    }

    @Override
    public Authentication authenticate(Authentication authentication) 
            throws AuthenticationException {
        
        String username = authentication.getName();
        Object credentials = authentication.getCredentials();
        
        if (credentials instanceof String) {
            String mfaCode = (String) credentials;
            
            if (verifyMfaCode(username, mfaCode)) {
                // MFA 验证成功，返回认证成功的 Authentication
                return createSuccessAuthentication(username, authentication);
            }
        }
        
        throw new org.springframework.security.authentication.BadCredentialsException(
                "Invalid MFA code");
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return MfaAuthenticationToken.class.isAssignableFrom(authentication);
    }

    /**
     * 创建成功的认证对象
     */
    private Authentication createSuccessAuthentication(String username, 
                                                   Authentication authentication) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new MfaAuthenticationToken(
                userDetails, 
                null, 
                userDetails.getAuthorities());
    }

    /**
     * 生成 secret ID
     */
    private String generateSecretId(String username) {
        return username + "-" + System.currentTimeMillis();
    }

    /**
     * 生成验证码（用于确认 secret）
     */
    private int generateVerificationCode(String secret) {
        return googleAuthenticator.calculateCode(secret, 
                System.currentTimeMillis() / 30000);
    }

    /**
     * 生成备用码
     */
    private String generateBackupCode() {
        return String.format("%s-%s-%s",
                getRandomCode(),
                getRandomCode(),
                getRandomCode());
    }

    /**
     * 生成随机码
     */
    private String getRandomCode() {
        return Integer.toHexString(
                new java.security.SecureRandom().nextInt(65536)).substring(0, 4);
    }

    /**
     * 临时 secret
     */
    private static class TempSecret {
        String secret;
        long expiresAt;

        TempSecret(String secret, long expiresAt) {
            this.secret = secret;
            this.expiresAt = expiresAt;
        }
    }

    /**
     * MFA Secret 信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MfaSecretInfo {
        private String secretId;
        private String secret;
        private String qrCodeUrl;
        private int verificationCode;
        private long expiresAt;
    }

    /**
     * 备用码
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class BackupCodes {
        private java.util.List<String> codes;
        private long expiresAt;
    }

    /**
     * MFA 认证 Token
     */
    public static class MfaAuthenticationToken extends 
            org.springframework.security.authentication.UsernamePasswordAuthenticationToken {
        
        public MfaAuthenticationToken(Object principal, Object credentials, 
                                      java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities) {
            super(principal, credentials, authorities);
        }
    }
}
