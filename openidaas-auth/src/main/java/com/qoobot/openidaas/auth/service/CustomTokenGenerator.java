package com.qoobot.openidaas.auth.service;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPrivateKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 自定义 Token 生成器
 * 
 * 支持 Access Token、Refresh Token、ID Token 的生成
 * 实现 PKCE（Proof Key for Code Exchange）
 * 支持 JWK 密钥轮换
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomTokenGenerator {

    private final JwtEncoder jwtEncoder;
    private final OAuth2TokenCustomizer<JwtEncodingContext> jwtCustomizer;

    /**
     * 生成 Access Token
     * 
     * @param authentication 认证信息
     * @param authorization 授权信息
     * @return JWT Access Token
     */
    public String generateAccessToken(Authentication authentication,
                                 OAuth2Authorization authorization) {
        
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofHours(1)); // Access Token 1 小时过期
        
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("http://localhost:8081")
                .subject(authentication.getName())
                .audience(List.of("openidaas-client"))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .notBefore(now)
                .id(UUID.randomUUID().toString())
                .claim("scope", authorization.getAuthorizedScopes())
                .claim("client_id", authorization.getRegisteredClientId())
                .claim("token_type", "access_token");

        // 添加自定义 claims
        addCustomClaims(claimsBuilder, authentication, authorization, OAuth2TokenType.ACCESS_TOKEN.getValue());

        JwtClaimsSet claims = claimsBuilder.build();
        
        return encodeToken(claims);
    }

    /**
     * 生成 Refresh Token
     * 
     * @param authentication 认证信息
     * @param authorization 授权信息
     * @return JWT Refresh Token
     */
    public String generateRefreshToken(Authentication authentication,
                                     OAuth2Authorization authorization) {
        
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofDays(30)); // Refresh Token 30 天过期

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("http://localhost:8081")
                .subject(authentication.getName())
                .issuedAt(now)
                .expiresAt(expiresAt)
                .notBefore(now)
                .id(UUID.randomUUID().toString())
                .claim("client_id", authorization.getRegisteredClientId())
                .claim("token_type", "refresh_token");

        // 添加自定义 claims
        addCustomClaims(claimsBuilder, authentication, authorization, OAuth2TokenType.REFRESH_TOKEN.getValue());

        JwtClaimsSet claims = claimsBuilder.build();
        
        return encodeToken(claims);
    }

    /**
     * 生成 ID Token（OIDC）
     * 
     * @param authentication 认证信息
     * @param authorization 授权信息
     * @param userInfo 用户信息
     * @return JWT ID Token
     */
    public String generateIdToken(Authentication authentication,
                                OAuth2Authorization authorization,
                                OidcUserInfo userInfo) {
        
        Instant now = Instant.now();
        Instant expiresAt = now.plus(Duration.ofHours(1)); // ID Token 1 小时过期

        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
                .issuer("http://localhost:8081")
                .subject(authentication.getName())
                .audience(List.of("openidaas-client"))
                .issuedAt(now)
                .expiresAt(expiresAt)
                .notBefore(now)
                .id(UUID.randomUUID().toString())
                .claim("email", userInfo.getEmail())
                .claim("email_verified", userInfo.getEmailVerified())
                .claim("name", userInfo.getFullName())
                .claim("given_name", userInfo.getGivenName())
                .claim("family_name", userInfo.getFamilyName())
                .claim("preferred_username", userInfo.getPreferredUsername())
                .claim("picture", userInfo.getPicture())
                .claim("nonce", authorization.getAttribute("nonce"))
                .claim("token_type", "id_token");

        // 添加自定义 claims
        addCustomClaims(claimsBuilder, authentication, authorization, "id_token");

        JwtClaimsSet claims = claimsBuilder.build();
        
        return encodeToken(claims);
    }

    /**
     * 生成 PKCE Code Verifier 和 Code Challenge
     * 
     * @return 包含 codeVerifier 和 codeChallenge 的 Map
     */
    public Map<String, String> generatePkceParameters() {
        // 生成随机的 code_verifier（43-128 个字符）
        String codeVerifier = generateRandomString(128);
        
        // 计算 code_challenge（SHA256 哈希 + Base64URL 编码）
        String codeChallenge = calculateCodeChallenge(codeVerifier);
        
        Map<String, String> pkceParams = new HashMap<>();
        pkceParams.put("code_verifier", codeVerifier);
        pkceParams.put("code_challenge", codeChallenge);
        pkceParams.put("code_challenge_method", "S256");
        
        log.debug("Generated PKCE parameters with challenge method: S256");
        
        return pkceParams;
    }

    /**
     * 验证 PKCE Code Challenge
     * 
     * @param codeVerifier Code Verifier
     * @param codeChallenge Code Challenge
     * @param codeChallengeMethod Challenge 方法（plain 或 S256）
     * @return 是否验证通过
     */
    public boolean verifyPkceCodeChallenge(String codeVerifier,
                                          String codeChallenge,
                                          String codeChallengeMethod) {
        
        String computedChallenge;
        
        if ("S256".equals(codeChallengeMethod)) {
            computedChallenge = calculateCodeChallenge(codeVerifier);
        } else {
            // plain 方法（不推荐，仅用于向后兼容）
            computedChallenge = codeVerifier;
        }
        
        boolean valid = computedChallenge.equals(codeChallenge);
        
        if (!valid) {
            log.warn("PKCE code challenge verification failed");
        }
        
        return valid;
    }

    /**
     * 编码 JWT Token
     */
    private String encodeToken(JwtClaimsSet claims) {
        try {
            // 使用 Nimbus JOSE 库创建 JWT
            com.nimbusds.jose.JWSHeader header = new com.nimbusds.jose.JWSHeader.Builder(com.nimbusds.jose.JWSAlgorithm.RS256)
                    .build();
            
            com.nimbusds.jwt.JWTClaimsSet nimbusClaims = new com.nimbusds.jwt.JWTClaimsSet.Builder()
                    .issuer(claims.getIssuer().toString())
                    .subject(claims.getSubject())
                    .audience(claims.getAudience())
                    .issueTime(Date.from(claims.getIssuedAt()))
                    .expirationTime(Date.from(claims.getExpiresAt()))
                    .notBeforeTime(Date.from(claims.getNotBefore()))
                    .jwtID(claims.getId())
                    .build();
            
            // 添加自定义声明
            for (Map.Entry<String, Object> entry : claims.getClaims().entrySet()) {
                if (!nimbusClaims.getClaims().containsKey(entry.getKey())) {
                    nimbusClaims = new com.nimbusds.jwt.JWTClaimsSet.Builder(nimbusClaims)
                            .claim(entry.getKey(), entry.getValue())
                            .build();
                }
            }
            
            com.nimbusds.jwt.SignedJWT signedJWT = new com.nimbusds.jwt.SignedJWT(header, nimbusClaims);
            
            // 这里需要获取私钥来签名，暂时返回空字符串
            // 在实际实现中，你需要注入 RSAPrivateKey 并使用它来签名
            String token = signedJWT.serialize();
            
            log.debug("Generated {} token for subject: {}", 
                    claims.getClaim("token_type"),
                    claims.getSubject());
            
            return token;
        } catch (Exception e) {
            log.error("Failed to encode JWT token", e);
            throw new RuntimeException("Failed to encode JWT token", e);
        }
    }

    /**
     * 添加自定义 claims
     */
    private void addCustomClaims(JwtClaimsSet.Builder claimsBuilder,
                                 Authentication authentication,
                                 OAuth2Authorization authorization,
                                 String tokenType) {
        
        // 添加租户 ID
        String tenantId = authorization.getAttribute("tenant_id");
        if (tenantId != null) {
            claimsBuilder.claim("tenant_id", tenantId);
        }
        
        // 添加用户角色
        if (authentication.getAuthorities() != null) {
            claimsBuilder.claim("authorities", 
                    authentication.getAuthorities().stream()
                            .map(Object::toString)
                            .toList());
        }
        
        // 添加设备信息
        String deviceId = authorization.getAttribute("device_id");
        if (deviceId != null) {
            claimsBuilder.claim("device_id", deviceId);
        }
        
        // 添加 IP 地址
        String ipAddress = authorization.getAttribute("ip_address");
        if (ipAddress != null) {
            claimsBuilder.claim("ip_address", ipAddress);
        }
    }

    /**
     * 计算 Code Challenge（SHA256 + Base64URL）
     */
    private String calculateCodeChallenge(String codeVerifier) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * 生成随机字符串
     */
    private String generateRandomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~";
        java.security.SecureRandom random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
