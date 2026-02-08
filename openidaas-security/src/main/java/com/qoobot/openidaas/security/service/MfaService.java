package com.qoobot.openidaas.security.service;

import com.qoobot.openidaas.security.config.SecurityProperties;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import com.warrenstrange.googleauth.GoogleAuthenticatorQRGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * MFA（多因子认证）服务
 * 
 * 基于Google Authenticator实现TOTP认证
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MfaService {

    private final SecurityProperties securityProperties;

    /**
     * 生成MFA密钥
     * 
     * @param username 用户名
     * @return MFA密钥信息
     */
    public MfaKeyInfo generateSecret(String username) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        GoogleAuthenticatorKey key = gAuth.createCredentials(username);

        String qrCodeUrl = GoogleAuthenticatorQRGenerator.getOtpAuthTotpURL(
                securityProperties.getMfa().getIssuer(),
                username,
                key
        );

        log.info("Generated MFA secret for user: {}", username);

        return MfaKeyInfo.builder()
                .secret(key.getKey())
                .qrCodeUrl(qrCodeUrl)
                .verificationCode(key.getVerificationCode())
                .scratchCodes(key.getScratchCodes())
                .build();
    }

    /**
     * 生成QR码图片
     * 
     * @param qrCodeUrl QR码URL
     * @param width 宽度
     * @param height 高度
     * @return QR码图片字节数组
     * @throws WriterException 写入异常
     * @throws IOException IO异常
     */
    public byte[] generateQrCodeImage(String qrCodeUrl, int width, int height)
            throws WriterException, IOException {

        BitMatrix bitMatrix = new MultiFormatWriter().encode(
                qrCodeUrl,
                BarcodeFormat.QR_CODE,
                width,
                height
        );

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

        return outputStream.toByteArray();
    }

    /**
     * 验证MFA验证码
     * 
     * @param secret 密钥
     * @param code 验证码
     * @return 是否验证通过
     */
    public boolean verifyCode(String secret, int code) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean valid = gAuth.authorize(secret, code);

        log.debug("MFA code verification result: {}", valid);
        return valid;
    }

    /**
     * 验证MFA验证码（带窗口期）
     * 
     * @param secret 密钥
     * @param code 验证码
     * @param window 窗口期大小
     * @return 是否验证通过
     */
    public boolean verifyCode(String secret, int code, int window) {
        GoogleAuthenticator gAuth = new GoogleAuthenticator();
        boolean valid = gAuth.authorize(secret, code, window);

        log.debug("MFA code verification result with window: {}", valid);
        return valid;
    }

    /**
     * 验证验证码格式
     * 
     * @param code 验证码
     * @return 是否有效
     */
    public boolean isValidCodeFormat(String code) {
        if (code == null || code.length() != securityProperties.getMfa().getCodeLength()) {
            return false;
        }

        try {
            Integer.parseInt(code);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * MFA密钥信息
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class MfaKeyInfo {
        /**
         * 密钥
         */
        private String secret;

        /**
         * QR码URL
         */
        private String qrCodeUrl;

        /**
         * 验证码（用于初始验证）
         */
        private Integer verificationCode;

        /**
         * 恢复码
         */
        private List<Integer> scratchCodes;
    }
}
