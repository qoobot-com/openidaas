package com.qoobot.openidaas.auth.security;

import com.qoobot.openidaas.core.entity.User;
import com.qoobot.openidaas.core.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 自定义用户详情服务
 * 
 * 实现 Spring Security 的 UserDetailsService 接口
 * 
 * @author Qoobot Team
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserService userService;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userService.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with username: " + username));
        
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.emptyList())
                .accountLocked(!user.getAccountNonLocked())
                .disabled(!user.getEnabled())
                .accountExpired(!user.getAccountNonExpired())
                .credentialsExpired(!user.getCredentialsNonExpired())
                .build();
    }
}
