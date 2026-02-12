package com.qoobot.openidaas.auth.config;

import com.qoobot.openidaas.auth.client.UserClient;
import com.qoobot.openidaas.auth.dto.UserInfoDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现
 * 从用户服务获取用户信息
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserClient userClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Loading user details for username: {}", username);

        try {
            UserInfoDTO userInfo = userClient.getUserByUsername(username);

            if (userInfo == null) {
                log.warn("User not found: {}", username);
                throw new UsernameNotFoundException("User not found: " + username);
            }

            if (!Boolean.TRUE.equals(userInfo.getEnabled())) {
                log.warn("User is disabled: {}", username);
                throw new UsernameNotFoundException("User is disabled: " + username);
            }

            if (!Boolean.TRUE.equals(userInfo.getAccountNonLocked())) {
                log.warn("User account is locked: {}", username);
                throw new UsernameNotFoundException("User account is locked: " + username);
            }

            if (!Boolean.TRUE.equals(userInfo.getAccountNonExpired())) {
                log.warn("User account is expired: {}", username);
                throw new UsernameNotFoundException("User account is expired: " + username);
            }

            if (!Boolean.TRUE.equals(userInfo.getCredentialsNonExpired())) {
                log.warn("User credentials are expired: {}", username);
                throw new UsernameNotFoundException("User credentials are expired: " + username);
            }

            List<SimpleGrantedAuthority> authorities = userInfo.getRoles() != null
                    ? userInfo.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                            .collect(Collectors.toList())
                    : Collections.emptyList();

            log.debug("User loaded successfully: {} with {} authorities", username, authorities.size());

            return User.withUsername(userInfo.getUsername())
                    .password(userInfo.getPassword())
                    .disabled(!Boolean.TRUE.equals(userInfo.getEnabled()))
                    .accountExpired(!Boolean.TRUE.equals(userInfo.getAccountNonExpired()))
                    .accountLocked(!Boolean.TRUE.equals(userInfo.getAccountNonLocked()))
                    .credentialsExpired(!Boolean.TRUE.equals(userInfo.getCredentialsNonExpired()))
                    .authorities(authorities)
                    .build();

        } catch (Exception e) {
            log.error("Error loading user details for username: {}", username, e);
            throw new UsernameNotFoundException("Error loading user details", e);
        }
    }
}
