package com.qoobot.openidaas.auth.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.Map;

/**
 * 登录页面控制器
 */
@Slf4j
@Controller
public class LoginController {

    /**
     * 登录页面
     */
    @GetMapping("/login")
    public String login(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {

        log.info("Login page request, error={}, logout={}", error, logout);

        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }

        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }

        return "login";
    }

    /**
     * 授权确认页面
     */
    @GetMapping("/oauth2/confirm_access")
    public String confirmAccess(Model model, Principal principal) {
        log.info("Authorization confirmation page for user: {}", principal.getName());

        // TODO: 获取授权请求信息
        model.addAttribute("clientId", "admin-client");
        model.addAttribute("scopes", new String[]{"openid", "profile", "email"});

        return "oauth2/confirm_access";
    }

    /**
     * 授权成功页面
     */
    @GetMapping("/oauth2/success")
    public String success(Model model, Principal principal) {
        log.info("Authorization success for user: {}", principal.getName());

        model.addAttribute("username", principal.getName());
        model.addAttribute("message", "Authorization successful");

        return "oauth2/success";
    }

    /**
     * 错误页面
     */
    @GetMapping("/error")
    public String error(Model model) {
        log.warn("Error page requested");

        model.addAttribute("message", "An error occurred");

        return "error";
    }
}
