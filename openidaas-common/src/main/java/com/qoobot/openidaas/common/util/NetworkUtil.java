package com.qoobot.openidaas.common.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Pattern;

/**
 * 网络工具类
 *
 * @author QooBot
 */
@Slf4j
@Component
public class NetworkUtil {

    private static final String UNKNOWN = "unknown";
    private static final String LOCALHOST = "127.0.0.1";
    private static final String LOCALHOST_IPV6 = "0:0:0:0:0:0:0:1";
    
    // IP地址正则表达式
    private static final Pattern IP_PATTERN = Pattern.compile(
        "^(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}$"
    );

    /**
     * 获取当前请求的IP地址
     */
    public static String getClientIpAddress() {
        try {
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                return LOCALHOST;
            }
            return getClientIpAddress(request);
        } catch (Exception e) {
            log.warn("Failed to get client IP address", e);
            return LOCALHOST;
        }
    }

    /**
     * 从HttpServletRequest获取客户端IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            return getFirstIp(ip);
        }

        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return getFirstIp(ip);
        }

        ip = request.getRemoteAddr();
        if (LOCALHOST_IPV6.equals(ip)) {
            ip = LOCALHOST;
        }
        
        return ip;
    }

    /**
     * 获取当前请求对象
     */
    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 验证IP地址是否有效
     */
    public static boolean isValidIp(String ip) {
        return ip != null && !UNKNOWN.equalsIgnoreCase(ip) && IP_PATTERN.matcher(ip).matches();
    }

    /**
     * 从多个IP中获取第一个有效的IP
     */
    private static String getFirstIp(String ips) {
        if (ips == null || ips.isEmpty()) {
            return null;
        }
        
        String[] ipArray = ips.split(",");
        for (String ip : ipArray) {
            ip = ip.trim();
            if (isValidIp(ip)) {
                return ip;
            }
        }
        return null;
    }

    /**
     * 获取本地主机IP地址
     */
    public static String getLocalHostIp() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostAddress();
        } catch (UnknownHostException e) {
            log.error("Failed to get local host IP", e);
            return LOCALHOST;
        }
    }

    /**
     * 获取本地主机名
     */
    public static String getLocalHostName() {
        try {
            InetAddress inetAddress = InetAddress.getLocalHost();
            return inetAddress.getHostName();
        } catch (UnknownHostException e) {
            log.error("Failed to get local host name", e);
            return "localhost";
        }
    }

    /**
     * 判断是否为内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (!isValidIp(ip)) {
            return false;
        }

        // 10.x.x.x
        if (ip.startsWith("10.")) {
            return true;
        }

        // 172.16.x.x - 172.31.x.x
        if (ip.startsWith("172.")) {
            String[] parts = ip.split("\\.");
            if (parts.length > 1) {
                int secondOctet = Integer.parseInt(parts[1]);
                return secondOctet >= 16 && secondOctet <= 31;
            }
        }

        // 192.168.x.x
        if (ip.startsWith("192.168.")) {
            return true;
        }

        // 127.x.x.x (localhost)
        if (ip.startsWith("127.")) {
            return true;
        }

        return false;
    }

    /**
     * 获取请求的User-Agent
     */
    public static String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader("User-Agent") : null;
    }

    /**
     * 获取请求的Referer
     */
    public static String getReferer() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader("Referer") : null;
    }

    /**
     * 获取请求URL
     */
    public static String getRequestUrl() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        
        StringBuffer url = request.getRequestURL();
        String queryString = request.getQueryString();
        if (queryString != null) {
            url.append("?").append(queryString);
        }
        return url.toString();
    }

    /**
     * 获取请求URI
     */
    public static String getRequestUri() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getRequestURI() : null;
    }

    /**
     * 获取请求方法
     */
    public static String getRequestMethod() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getMethod() : null;
    }
}