package com.qoobot.openidaas.auth.config;

import com.qoobot.openidaas.auth.security.JwtAuthenticationFilter;
import com.qoobot.openidaas.auth.security.JwtUtils;
import com.qoobot.openidaas.auth.provider.MfaAuthenticationProvider;
import com.qoobot.openidaas.auth.provider.SocialLoginProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import com.qoobot.openidaas.core.constants.SecurityConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * Spring Security 配置类
 * 
 * 配置 OAuth2.1/OIDC 认证和授权
 * 支持 MFA、社会化登录、PKCE
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MfaAuthenticationProvider mfaAuthenticationProvider;
    private final SocialLoginProvider socialLoginProvider;
    
    /**
     * 密码编码器
     * 使用 BCrypt + Argon2（如果可用）
     */
    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        try {
            // 尝试使用 Argon2
            Class<?> argon2Class = Class.forName("de.mkammerer.argon2.Argon2Factory");
            if (argon2Class != null) {
                // Argon2 配置
                return new Argon2PasswordEncoder();
            }
        } catch (Exception e) {
            // 回退到 BCrypt
            log.warn("Argon2 not available, falling back to BCrypt");
        }
        
        // BCrypt 配置
        return new BCryptPasswordEncoder(12);
    }
    
    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    /**
     * DAO 认证提供者（用户名密码认证）
     */
    @Bean
    public AuthenticationProvider daoAuthenticationProvider(PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }
    
    /**
     * 安全过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF（使用 JWT 时不需要）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置 CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理为无状态
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 公开端点
                .requestMatchers(
                    "/api/auth/login",
                    "/api/auth/register",
                    "/api/auth/refresh",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password",
                    "/api/auth/social/**",
                    "/oauth2/**",
                    "/.well-known/**",
                    "/error",
                    "/actuator/**",
                    "/swagger-ui/**",
                    "/v3/api-docs/**"
                ).permitAll()
                
                // 管理员端点
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            
            // 配置认证提供者
            .authenticationProvider(mfaAuthenticationProvider)
            .authenticationProvider(socialLoginProvider)
            
            // 添加 JWT 过滤器
            .addFilterBefore(jwtAuthenticationFilter, 
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * CORS 配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    
    /**
     * Argon2 密码编码器（可选）
     */
    private static class Argon2PasswordEncoder implements PasswordEncoder {
        
        public Argon2PasswordEncoder() {
            // 无参数构造函数
        }
        
        @Override
        public String encode(CharSequence rawPassword) {
            try {
                Class<?> argon2Class = Class.forName("de.mkammerer.argon2.Argon2Factory");
                Object argon2Instance = argon2Class.getMethod("create").invoke(null);
                return (String) argon2Instance.getClass()
                    .getMethod("hash", int.class, int.class, int.class, String.class)
                    .invoke(argon2Instance, 3, 65536, 1, rawPassword.toString());
            } catch (Exception e) {
                throw new RuntimeException("Failed to encode password with Argon2", e);
            }
        }
        
        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            try {
                Class<?> argon2Class = Class.forName("de.mkammerer.argon2.Argon2Factory");
                Object argon2Instance = argon2Class.getMethod("create").invoke(null);
                return (Boolean) argon2Instance.getClass()
                    .getMethod("verify", String.class, String.class)
                    .invoke(argon2Instance, encodedPassword, rawPassword.toString());
            } catch (Exception e) {
                throw new RuntimeException("Failed to verify password with Argon2", e);
            }
        }
    }
}
