package com.qoobot.openidaas.audit.aspect;

import com.qoobot.openidaas.audit.service.AuditService;
import com.qoobot.openidaas.common.dto.audit.AuditLogCreateDTO;
import com.qoobot.openidaas.common.enumeration.OperationTypeEnum;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * 审计日志切面测试
 *
 * @author QooBot
 */
@ExtendWith(MockitoExtension.class)
class AuditLogAspectTest {

    @Mock
    private AuditService auditService;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private MethodSignature methodSignature;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ServletRequestAttributes requestAttributes;

    @InjectMocks
    private AuditLogAspect auditLogAspect;

    private AuditLogAspect.Audit auditAnnotation;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        // 准备审计注解
        Method method = this.getClass().getMethod("testMethod");
        auditAnnotation = method.getAnnotation(AuditLogAspect.Audit.class);
    }

    @Test
    void testAuditAnnotation() {
        // 验证注解存在
        assertNotNull(auditAnnotation);
        assertEquals(OperationTypeEnum.READ, auditAnnotation.operationType());
        assertEquals("测试方法", auditAnnotation.description());
        assertEquals("TEST", auditAnnotation.module());
    }

    @Test
    void testAround_Success() throws Throwable {
        // 准备
        Object[] args = new Object[]{"arg1", "arg2"};
        Object result = "success";
        Method testMethod = this.getClass().getMethod("testMethod");

        // Mock行为
        when(joinPoint.proceed()).thenReturn(result);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(testMethod);

        try (MockedStatic<RequestContextHolder> requestContextHolderMock = mockStatic(RequestContextHolder.class)) {
            requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(requestAttributes.getRequest()).thenReturn(request);
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("GET");
            when(request.getRemoteAddr()).thenReturn("192.168.1.1");
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

            doNothing().when(auditService).recordAuditLog(any(AuditLogCreateDTO.class));

            // 执行
            Object resultObj = auditLogAspect.around(joinPoint, auditAnnotation);

            // 验证
            assertNotNull(resultObj);
            assertEquals("success", resultObj);
            verify(auditService, times(1)).recordAuditLog(any(AuditLogCreateDTO.class));
        }
    }

    @Test
    void testAround_WithException() throws Throwable {
        // 准备
        Object[] args = new Object[]{"arg1"};
        Exception exception = new RuntimeException("Test exception");
        Method testMethod = this.getClass().getMethod("testMethod");

        // Mock行为
        when(joinPoint.proceed()).thenThrow(exception);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(testMethod);

        try (MockedStatic<RequestContextHolder> requestContextHolderMock = mockStatic(RequestContextHolder.class)) {
            requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(requestAttributes.getRequest()).thenReturn(request);
            when(request.getRequestURI()).thenReturn("/api/test");
            when(request.getMethod()).thenReturn("GET");

            doNothing().when(auditService).recordAuditLog(any(AuditLogCreateDTO.class));

            // 执行和验证
            assertThrows(RuntimeException.class, () -> auditLogAspect.around(joinPoint, auditAnnotation));

            // 验证
            verify(auditService, times(1)).recordAuditLog(any(AuditLogCreateDTO.class));
        }
    }

    @Test
    void testAround_AsyncMode() throws Throwable {
        // 准备
        Object[] args = new Object[]{"arg1"};
        Object result = "success";
        Method testMethod = this.getClass().getMethod("testMethodAsync");

        // 获取async注解
        AuditLogAspect.Audit asyncAnnotation = testMethod.getAnnotation(AuditLogAspect.Audit.class);

        // Mock行为
        when(joinPoint.proceed()).thenReturn(result);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(testMethod);

        try (MockedStatic<RequestContextHolder> requestContextHolderMock = mockStatic(RequestContextHolder.class)) {
            requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(requestAttributes.getRequest()).thenReturn(request);
            when(request.getRequestURI()).thenReturn("/api/test");

            doNothing().when(auditService).sendAuditLogAsync(any(AuditLogCreateDTO.class));

            // 执行
            Object resultObj = auditLogAspect.around(joinPoint, asyncAnnotation);

            // 验证
            assertNotNull(resultObj);
            assertEquals("success", resultObj);
            verify(auditService, times(1)).sendAuditLogAsync(any(AuditLogCreateDTO.class));
            verify(auditService, never()).recordAuditLog(any(AuditLogCreateDTO.class));
        }
    }

    @Test
    void testAround_RecordParamsFalse() throws Throwable {
        // 准备
        Object[] args = new Object[]{"sensitive_data"};
        Object result = "success";
        Method testMethod = this.getClass().getMethod("testMethodNoParams");

        AuditLogAspect.Audit noParamsAnnotation = testMethod.getAnnotation(AuditLogAspect.Audit.class);

        // Mock行为
        when(joinPoint.proceed()).thenReturn(result);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(testMethod);

        try (MockedStatic<RequestContextHolder> requestContextHolderMock = mockStatic(RequestContextHolder.class)) {
            requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(requestAttributes.getRequest()).thenReturn(request);
            when(request.getRequestURI()).thenReturn("/api/test");

            doNothing().when(auditService).recordAuditLog(any(AuditLogCreateDTO.class));

            // 执行
            auditLogAspect.around(joinPoint, noParamsAnnotation);

            // 验证
            verify(auditService, times(1)).recordAuditLog(argThat(dto -> {
                // 验证请求参数未记录
                return dto.getRequestParams() == null;
            }));
        }
    }

    @Test
    void testAround_RecordResultTrue() throws Throwable {
        // 准备
        Object[] args = new Object[]{"arg1"};
        Object result = new Object() {
            @Override
            public String toString() {
                return "result_data";
            }
        };
        Method testMethod = this.getClass().getMethod("testMethodWithResult");

        AuditLogAspect.Audit withResultAnnotation = testMethod.getAnnotation(AuditLogAspect.Audit.class);

        // Mock行为
        when(joinPoint.proceed()).thenReturn(result);
        when(joinPoint.getArgs()).thenReturn(args);
        when(joinPoint.getSignature()).thenReturn(methodSignature);
        when(methodSignature.getMethod()).thenReturn(testMethod);

        try (MockedStatic<RequestContextHolder> requestContextHolderMock = mockStatic(RequestContextHolder.class)) {
            requestContextHolderMock.when(RequestContextHolder::getRequestAttributes).thenReturn(requestAttributes);
            when(requestAttributes.getRequest()).thenReturn(request);
            when(request.getRequestURI()).thenReturn("/api/test");

            doNothing().when(auditService).recordAuditLog(any(AuditLogCreateDTO.class));

            // 执行
            auditLogAspect.around(joinPoint, withResultAnnotation);

            // 验证
            verify(auditService, times(1)).recordAuditLog(argThat(dto -> {
                // 验证响应结果已记录
                return dto.getResponseResult() != null && dto.getResponseResult().contains("result_data");
            }));
        }
    }

    @Test
    void testGetClientIp_WithXForwardedFor() {
        // 准备
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 198.51.100.1");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // 执行 - 需要通过反射调用私有方法
        try {
            java.lang.reflect.Method method = AuditLogAspect.class.getDeclaredMethod("getClientIp", HttpServletRequest.class);
            method.setAccessible(true);
            String ip = (String) method.invoke(auditLogAspect, request);

            // 验证
            assertEquals("203.0.113.1", ip);
        } catch (Exception e) {
            fail("测试失败: " + e.getMessage());
        }
    }

    @Test
    void testGetClientIp_WithoutXForwardedFor() {
        // 准备
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn(null);
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // 执行 - 需要通过反射调用私有方法
        try {
            java.lang.reflect.Method method = AuditLogAspect.class.getDeclaredMethod("getClientIp", HttpServletRequest.class);
            method.setAccessible(true);
            String ip = (String) method.invoke(auditLogAspect, request);

            // 验证
            assertEquals("192.168.1.1", ip);
        } catch (Exception e) {
            fail("测试失败: " + e.getMessage());
        }
    }

    @Test
    void testGetClientIp_WithXRealIP() {
        // 准备
        when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        when(request.getHeader("X-Real-IP")).thenReturn("203.0.113.1");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");

        // 执行 - 需要通过反射调用私有方法
        try {
            java.lang.reflect.Method method = AuditLogAspect.class.getDeclaredMethod("getClientIp", HttpServletRequest.class);
            method.setAccessible(true);
            String ip = (String) method.invoke(auditLogAspect, request);

            // 验证
            assertEquals("203.0.113.1", ip);
        } catch (Exception e) {
            fail("测试失败: " + e.getMessage());
        }
    }

    // 测试方法 - 带有@Audit注解
    @AuditLogAspect.Audit(
        operationType = OperationTypeEnum.READ,
        description = "测试方法",
        module = "TEST"
    )
    public void testMethod() {
    }

    @AuditLogAspect.Audit(
        operationType = OperationTypeEnum.READ,
        description = "异步测试",
        module = "TEST",
        async = true
    )
    public void testMethodAsync() {
    }

    @AuditLogAspect.Audit(
        operationType = OperationTypeEnum.READ,
        description = "不记录参数",
        module = "TEST",
        recordParams = false
    )
    public void testMethodNoParams() {
    }

    @AuditLogAspect.Audit(
        operationType = OperationTypeEnum.READ,
        description = "记录结果",
        module = "TEST",
        recordResult = true
    )
    public Object testMethodWithResult() {
        return new Object();
    }
}
