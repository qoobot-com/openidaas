package com.qoobot.openidaas.user.controller;

import com.qoobot.openidaas.core.dto.UserDTO;
import com.qoobot.openidaas.core.entity.User;
import com.qoobot.openidaas.core.service.UserService;
import com.qoobot.openidaas.user.dto.CreateUserRequest;
import com.qoobot.openidaas.user.dto.UpdateUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器（已废弃，请使用 UserManagementController）
 * 
 * @author Qoobot Team
 * @since 1.0.0
 * @deprecated 请使用 UserManagementController
 */
@Deprecated
@RestController
@RequestMapping("/api/users/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.toEntity(request);
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.toDTO(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) {
        User user = userService.findUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(userService.toDTO(user));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserDTO>> getAllUsers(Pageable pageable) {
        Page<User> users = userService.findAllUsers(pageable);
        return ResponseEntity.ok(users.map(userService::toDTO));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request) {
        User user = userService.findUserById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        userService.updateUserFromDTO(user, request);
        User updated = userService.updateUser(user);
        return ResponseEntity.ok(userService.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
