package com.qoobot.openidaas.security.config;

import com.qoobot.openidaas.security.filter.JwtAuthenticationFilter;
import com.qoobot.openidaas.security.filter.RateLimitingFilter;
import com.qoobot.openidaas.security.handler.CustomAccessDeniedHandler;
import com.qoobot.openidaas.security.handler.CustomAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
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

/**
 * 安全配置类
 * 
 * 提供完整的Spring Security配置，包括JWT认证、限流、CORS等
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final SecurityProperties securityProperties;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 安全过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF（使用JWT）
            .csrf(AbstractHttpConfigurer::disable)
            
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理（无状态）
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // 配置异常处理
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            )
            
            // 配置授权规则
            .authorizeHttpRequests(auth -> auth
                // 公开端点
                .requestMatchers(
                    "/api/auth/**",
                    "/api/public/**",
                    "/actuator/health",
                    "/actuator/info",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/webjars/**"
                ).permitAll()
                
                // 管理端点需要管理员权限
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                
                // 其他请求需要认证
                .anyRequest().authenticated()
            )
            
            // 配置安全头
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp.policyDirectives(getContentSecurityPolicy()))
                .frameOptions(frame -> frame.deny())
                .xssProtection(xss -> xss.headerValue(org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED))
                .httpStrictTransportSecurity(hsts -> 
                    hsts.includeSubDomains(true).maxAgeInSeconds(31536000)
                )
                .contentTypeOptions(content -> content.disable())
            );
        
        // 添加限流过滤器
        http.addFilterBefore(rateLimitingFilter, UsernamePasswordAuthenticationFilter.class);
        
        // 添加JWT过滤器
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    /**
     * CORS配置源
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        SecurityProperties.Cors corsConfig = securityProperties.getCors();
        
        if (corsConfig.getEnabled()) {
            configuration.setAllowedOrigins(Arrays.asList(corsConfig.getAllowedOrigins()));
            configuration.setAllowedMethods(Arrays.asList(corsConfig.getAllowedMethods()));
            configuration.setAllowedHeaders(Arrays.asList(corsConfig.getAllowedHeaders()));
            configuration.setAllowCredentials(corsConfig.getAllowCredentials());
            configuration.setMaxAge(corsConfig.getMaxAge());
        }
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 内容安全策略
     */
    private String getContentSecurityPolicy() {
        return "default-src 'self'; " +
               "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
               "style-src 'self' 'unsafe-inline'; " +
               "img-src 'self' data: https:; " +
               "font-src 'self' data:; " +
               "connect-src 'self'; " +
               "frame-ancestors 'none';";
    }
}
