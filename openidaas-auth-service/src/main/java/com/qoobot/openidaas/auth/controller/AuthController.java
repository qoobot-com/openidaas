package com.qoobot.openidaas.auth.controller;

import com.qoobot.openidaas.auth.service.MFAService;
import com.qoobot.openidaas.common.dto.auth.RefreshTokenDTO;
import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.auth.util.JwtUtil;
import com.qoobot.openidaas.common.util.PasswordUtil;
import com.qoobot.openidaas.common.vo.ResultVO;
import com.qoobot.openidaas.auth.vo.LoginVO;
import com.qoobot.openidaas.auth.client.UserClient;
import com.qoobot.openidaas.auth.dto.UserInfoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证服务Controller
 *
 * @author QooBot
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "认证服务", description = "认证相关接口")
public class AuthController {

    private final UserClient userClient;
    private final JwtUtil jwtUtil;
    private final MFAService mfaService;

    @Value("${app.jwt.secret}")
    private String jwtSecret;

    @Value("${app.jwt.access-token-validity:3600}")
    private long accessTokenValidity;

    @Value("${app.jwt.refresh-token-validity:2592000}")
    private long refreshTokenValidity;

    @Value("${app.mfa.issuer:IDaaS}")
    private String mfaIssuer;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户通过用户名密码进行登录认证，支持多因子认证")
    public ResultVO<LoginVO> login(@Valid @RequestBody LoginRequest request) {
        // 查询用户
        UserInfoDTO user = userClient.getUserByUsername(request.getUsername());
        if (user == null) {
            // 模拟增加失败尝试次数
            throw new BusinessException("用户名或密码错误");
        }

        // 检查用户状态
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new BusinessException("账户已被禁用");
        }
        if (!Boolean.TRUE.equals(user.getAccountNonLocked())) {
            throw new BusinessException("账户已被锁定，请联系管理员");
        }
        if (!Boolean.TRUE.equals(user.getAccountNonExpired())) {
            throw new BusinessException("账户已过期");
        }
        if (!Boolean.TRUE.equals(user.getCredentialsNonExpired())) {
            throw new BusinessException("凭证已过期");
        }

        // 验证密码（这里简化处理，实际应该调用密码验证服务）
         boolean isValid = PasswordUtil.verifyPassword(request.getPassword(), user.getPasswordHash());
        if (!isValid) {
            // 模拟增加失败尝试次数
            throw new BusinessException("用户名或密码错误");
        }

        // 检查MFA
        if (mfaService.isMFAEnabled(user.getId())) {
            // 用户启用了MFA，必须验证MFA代码
            if (request.getMfaCode() == null || request.getMfaCode().isEmpty()) {
                throw new BusinessException("请输入MFA验证码");
            }

            // 验证MFA代码
            boolean mfaValid = mfaService.verifyMFA(user.getId(), request.getMfaCode(), request.getClientIp());
            if (!mfaValid) {
                throw new BusinessException("MFA验证码错误");
            }
        }

        // 生成Token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());

        String accessToken = jwtUtil.generateToken(user.getUsername(), claims, accessTokenValidity);
        String refreshToken = jwtUtil.generateToken(user.getUsername() + ":refresh", claims, refreshTokenValidity);

        // 更新最后登录信息（通过Feign客户端调用）
        // userClient.updateLastLoginInfo(user.getId(), request.getClientIp());

        // 构建返回结果
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setExpiresIn(accessTokenValidity);
        loginVO.setTokenType("Bearer");

        // 设置用户信息
        LoginVO.UserInfo userInfo = new LoginVO.UserInfo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setEmail(user.getEmail());
        loginVO.setUserInfo(userInfo);

        log.info("用户登录成功, userId: {}, username: {}", user.getId(), user.getUsername());
        return ResultVO.success(loginVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户主动登出，使当前会话失效")
    public ResultVO<Void> logout(@RequestHeader("Authorization") String authorization) {
        // TODO: 将Token加入黑名单
        log.info("用户登出");
        return ResultVO.success();
    }

    /**
     * 刷新访问令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新访问令牌", description = "使用刷新令牌获取新的访问令牌")
    public ResultVO<Map<String, Object>> refreshToken(@Valid @RequestBody RefreshTokenDTO refreshTokenDTO) {
        try {
            // 验证刷新令牌
            String username = jwtUtil.extractUsername(refreshTokenDTO.getRefreshToken());
            if (!jwtUtil.validateToken(refreshTokenDTO.getRefreshToken(), username)) {
                throw new BusinessException("刷新令牌无效或已过期");
            }

            // 查询用户
            UserInfoDTO user = userClient.getUserByUsername(username);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 检查用户状态
            if (!Boolean.TRUE.equals(user.getEnabled())) {
                throw new BusinessException("账户已被禁用");
            }

            // 生成新的访问令牌
            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", user.getId());
            claims.put("username", user.getUsername());
            claims.put("email", user.getEmail());

            String newAccessToken = jwtUtil.generateToken(username, claims, accessTokenValidity);

            Map<String, Object> result = new HashMap<>();
            result.put("accessToken", newAccessToken);
            result.put("expiresIn", accessTokenValidity);

            return ResultVO.success(result);
        } catch (Exception e) {
            throw new BusinessException("刷新令牌失败: " + e.getMessage());
        }
    }

    /**
     * 生成MFA设置信息（TOTP）
     */
    @PostMapping("/mfa/setup/totp")
    @Operation(summary = "生成TOTP设置信息", description = "为用户生成TOTP设置信息，包含密钥和二维码")
    public ResultVO<Map<String, Object>> generateTOTPSetup(@RequestHeader("X-User-Id") Long userId) {
        Map<String, Object> result = mfaService.generateTOTPSetup(userId, mfaIssuer);
        return ResultVO.success(result);
    }

    /**
     * 验证并激活TOTP
     */
    @PostMapping("/mfa/activate/totp")
    @Operation(summary = "验证并激活TOTP", description = "验证TOTP代码并激活MFA功能")
    public ResultVO<Void> activateTOTP(@RequestHeader("X-User-Id") Long userId,
                                       @Valid @RequestBody ActivateTOTPRequest request) {
        boolean success = mfaService.verifyAndActivateTOTP(userId, request.getSecret(), request.getCode());
        if (!success) {
            throw new BusinessException("TOTP验证失败");
        }
        return ResultVO.success();
    }

    /**
     * 发送短信验证码
     */
    @PostMapping("/mfa/send-sms")
    @Operation(summary = "发送短信验证码", description = "为用户发送短信验证码")
    public ResultVO<Void> sendSMSCode(@RequestHeader("X-User-Id") Long userId,
                                       @Valid @RequestBody SendSMSRequest request) {
        boolean success = mfaService.sendSMSCode(userId, request.getPhoneNumber());
        if (!success) {
            throw new BusinessException("短信发送失败");
        }
        return ResultVO.success();
    }

    /**
     * 发送邮箱验证码
     */
    @PostMapping("/mfa/send-email")
    @Operation(summary = "发送邮箱验证码", description = "为用户发送邮箱验证码")
    public ResultVO<Void> sendEmailCode(@RequestHeader("X-User-Id") Long userId,
                                         @Valid @RequestBody SendEmailRequest request) {
        boolean success = mfaService.sendEmailCode(userId, request.getEmail());
        if (!success) {
            throw new BusinessException("邮件发送失败");
        }
        return ResultVO.success();
    }

    /**
     * 生成备用码
     */
    @PostMapping("/mfa/backup-codes")
    @Operation(summary = "生成备用码", description = "为用户生成备用验证码")
    public ResultVO<Map<String, Object>> generateBackupCodes(@RequestHeader("X-User-Id") Long userId,
                                                                @RequestParam(defaultValue = "10") int count) {
        Map<String, Object> result = mfaService.generateBackupCodes(userId, count);
        return ResultVO.success(result);
    }

    /**
     * 获取用户的MFA偏好设置
     */
    @GetMapping("/mfa/preferences")
    @Operation(summary = "获取MFA偏好设置", description = "获取用户的多因子认证偏好设置")
    public ResultVO<Map<String, Object>> getMFAPreferences(@RequestHeader("X-User-Id") Long userId) {
        Map<String, Object> result = mfaService.getUserMFAPreferences(userId);
        return ResultVO.success(result);
    }

    /**
     * 禁用MFA因子
     */
    @DeleteMapping("/mfa/factors/{factorId}")
    @Operation(summary = "禁用MFA因子", description = "禁用指定的MFA认证因子")
    public ResultVO<Void> disableMFAFactor(@RequestHeader("X-User-Id") Long userId,
                                           @PathVariable Long factorId) {
        boolean success = mfaService.disableMFAPreference(userId, factorId);
        if (!success) {
            throw new BusinessException("禁用MFA因子失败");
        }
        return ResultVO.success();
    }

    /**
     * 设置主MFA方式
     */
    @PutMapping("/mfa/factors/{factorId}/primary")
    @Operation(summary = "设置主MFA方式", description = "设置指定的MFA因子为主认证方式")
    public ResultVO<Void> setPrimaryMFA(@RequestHeader("X-User-Id") Long userId,
                                          @PathVariable Long factorId) {
        boolean success = mfaService.setPrimaryMFA(userId, factorId);
        if (!success) {
            throw new BusinessException("设置主MFA失败");
        }
        return ResultVO.success();
    }

    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "管理员重置用户密码或用户自助重置密码")
    public ResultVO<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        // TODO: 实现密码重置逻辑
        return ResultVO.success();
    }

    /**
     * 登录请求
     */
    public static class LoginRequest {
        private String username;
        private String password;
        private String mfaCode;
        private String clientIp;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getMfaCode() {
            return mfaCode;
        }

        public void setMfaCode(String mfaCode) {
            this.mfaCode = mfaCode;
        }

        public String getClientIp() {
            return clientIp;
        }

        public void setClientIp(String clientIp) {
            this.clientIp = clientIp;
        }
    }

    /**
     * 激活TOTP请求
     */
    public static class ActivateTOTPRequest {
        private String secret;
        private String code;

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }
    }

    /**
     * 发送短信请求
     */
    public static class SendSMSRequest {
        private String phoneNumber;

        public String getPhoneNumber() {
            return phoneNumber;
        }

        public void setPhoneNumber(String phoneNumber) {
            this.phoneNumber = phoneNumber;
        }
    }

    /**
     * 发送邮件请求
     */
    public static class SendEmailRequest {
        private String email;

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    /**
     * 重置密码请求
     */
    public static class ResetPasswordRequest {
        private Long userId;
        private String oldPassword;
        private String newPassword;
        private String confirmPassword;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }
}
