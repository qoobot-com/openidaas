package com.qoobot.openidaas.common.feign;

import com.qoobot.openidaas.common.exception.BusinessException;
import com.qoobot.openidaas.common.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;

/**
 * Feign 调用辅助工具类
 *
 * @author QooBot
 */
@Slf4j
public class FeignHelper {

    /**
     * 安全调用 Feign 客户端，自动处理响应和异常
     *
     * @param callable Feign 调用
     * @param <T>      返回数据类型
     * @return 响应数据
     * @throws BusinessException 业务异常
     */
    public static <T> T call(SafeFeignCallable<ResultVO<T>> callable) {
        try {
            ResultVO<T> result = callable.call();
            if (result == null) {
                log.error("Feign 调用返回 null");
                throw new BusinessException("服务调用失败");
            }
            if (!result.isSuccess()) {
                log.error("Feign 调用失败 - Code: {}, Message: {}", result.getCode(), result.getMessage());
                throw new BusinessException(result.getMessage());
            }
            return result.getData();
        } catch (Exception e) {
            log.error("Feign 调用异常", e);
            if (e instanceof BusinessException) {
                throw (BusinessException) e;
            }
            throw new BusinessException("服务调用异常: " + e.getMessage());
        }
    }

    /**
     * 异步调用 Feign 客户端
     *
     * @param callable Feign 调用
     * @param <T>      返回数据类型
     * @return 异步结果
     */
    public static <T> java.util.concurrent.CompletableFuture<T> callAsync(
            SafeFeignCallable<ResultVO<T>> callable) {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> call(callable));
    }

    /**
     * 带重试的 Feign 调用
     *
     * @param callable Feign 调用
     * @param retries  重试次数
     * @param <T>      返回数据类型
     * @return 响应数据
     */
    public static <T> T callWithRetry(SafeFeignCallable<ResultVO<T>> callable, int retries) {
        int attempt = 0;
        while (attempt <= retries) {
            try {
                return call(callable);
            } catch (Exception e) {
                attempt++;
                if (attempt > retries) {
                    log.error("Feign 调用失败，已重试 {} 次", retries, e);
                    throw new BusinessException("服务调用失败: " + e.getMessage());
                }
                log.warn("Feign 调用失败，第 {} 次重试", attempt);
                try {
                    Thread.sleep(1000 * attempt); // 指数退避
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new BusinessException("调用被中断");
                }
            }
        }
        throw new BusinessException("服务调用失败");
    }

    /**
     * 安全调用接口
     *
     * @param <T> 返回类型
     */
    @FunctionalInterface
    public interface SafeFeignCallable<T> {
        T call() throws Exception;
    }
}
