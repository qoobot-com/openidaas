package com.qoobot.openidaas.auth.util;

import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * TOTP（基于时间的一次性密码）工具类
 *
 * @author QooBot
 */
public class TOTPUtil {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
    private static final int SECRET_SIZE = 20; // 160 bits
    private static final int CODE_DIGITS = 6;
    private static final int TIME_STEP_SECONDS = 30; // 30秒有效期
    private static final int WINDOW_SIZE = 1; // 允许时间窗口偏移（前后各1个时间步）

    /**
     * 生成随机密钥
     *
     * @return Base32编码的密钥
     */
    public static String generateSecret() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[SECRET_SIZE];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }

    /**
     * 生成TOTP验证码
     *
     * @param secret Base32编码的密钥
     * @return 6位数字验证码
     */
    public static String generateTOTP(String secret) {
        return generateTOTP(secret, Instant.now());
    }

    /**
     * 生成指定时间的TOTP验证码
     *
     * @param secret Base32编码的密钥
     * @param time 时间点
     * @return 6位数字验证码
     */
    public static String generateTOTP(String secret, Instant time) {
        Base32 base32 = new Base32();
        byte[] key = base32.decode(secret);

        long timeStep = time.getEpochSecond() / TIME_STEP_SECONDS;
        byte[] data = new byte[8];
        for (int i = 7; i >= 0; i--) {
            data[i] = (byte) (timeStep & 0xff);
            timeStep >>= 8;
        }

        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            SecretKeySpec signingKey = new SecretKeySpec(key, HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            byte[] hash = mac.doFinal(data);

            int offset = hash[hash.length - 1] & 0xf;
            int binary = ((hash[offset] & 0x7f) << 24)
                    | ((hash[offset + 1] & 0xff) << 16)
                    | ((hash[offset + 2] & 0xff) << 8)
                    | (hash[offset + 3] & 0xff);

            int otp = binary % ((int) Math.pow(10, CODE_DIGITS));
            return String.format("%0" + CODE_DIGITS + "d", otp);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate TOTP", e);
        }
    }

    /**
     * 验证TOTP验证码
     *
     * @param secret Base32编码的密钥
     * @param code 验证码
     * @return 是否验证成功
     */
    public static boolean verify(String secret, String code) {
        return verify(secret, code, Instant.now());
    }

    /**
     * 验证指定时间的TOTP验证码
     *
     * @param secret Base32编码的密钥
     * @param code 验证码
     * @param time 时间点
     * @return 是否验证成功
     */
    public static boolean verify(String secret, String code, Instant time) {
        // 检查前后各一个时间窗口
        for (int i = -WINDOW_SIZE; i <= WINDOW_SIZE; i++) {
            Instant windowTime = time.plus(Duration.ofSeconds(i * TIME_STEP_SECONDS));
            String expectedCode = generateTOTP(secret, windowTime);
            if (expectedCode.equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 生成Google Authenticator的otpauth URI
     *
     * @param secret Base32编码的密钥
     * @param account 账户名称（通常是邮箱）
     * @param issuer 颁发者名称
     * @return otpauth URI
     */
    public static String generateOtpAuthURI(String secret, String account, String issuer) {
        return String.format("otpauth://totp/%s:%s?secret=%s&issuer=%s&digits=%d",
                issuer, account, secret, issuer, CODE_DIGITS);
    }

    /**
     * 计算剩余有效期秒数
     *
     * @return 剩余秒数
     */
    public static long getRemainingSeconds() {
        long now = Instant.now().getEpochSecond();
        long step = now / TIME_STEP_SECONDS;
        long nextStep = (step + 1) * TIME_STEP_SECONDS;
        return nextStep - now;
    }
}
