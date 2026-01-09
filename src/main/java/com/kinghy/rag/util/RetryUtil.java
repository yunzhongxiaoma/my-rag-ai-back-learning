package com.kinghy.rag.util;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

/**
 * 重试工具类
 * 
 * @author yunzhongxiaoma
 */
@Slf4j
public class RetryUtil {

    /**
     * 执行带重试的操作
     * 
     * @param operation 要执行的操作
     * @param maxRetries 最大重试次数
     * @param retryDelay 重试间隔（毫秒）
     * @param operationName 操作名称（用于日志）
     * @param <T> 返回类型
     * @return 操作结果
     * @throws Exception 如果所有重试都失败
     */
    public static <T> T executeWithRetry(Supplier<T> operation, int maxRetries, long retryDelay, String operationName) throws Exception {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries + 1; attempt++) {
            try {
                T result = operation.get();
                if (attempt > 1) {
                    log.info("Operation '{}' succeeded on attempt {}", operationName, attempt);
                }
                return result;
            } catch (Exception e) {
                lastException = e;
                
                if (attempt <= maxRetries) {
                    log.warn("Operation '{}' failed on attempt {} of {}: {}. Retrying in {}ms...", 
                            operationName, attempt, maxRetries + 1, e.getMessage(), retryDelay);
                    
                    try {
                        Thread.sleep(retryDelay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                } else {
                    log.error("Operation '{}' failed after {} attempts", operationName, maxRetries + 1, e);
                }
            }
        }
        
        throw lastException;
    }

    /**
     * 执行带重试的操作（无返回值）
     * 
     * @param operation 要执行的操作
     * @param maxRetries 最大重试次数
     * @param retryDelay 重试间隔（毫秒）
     * @param operationName 操作名称（用于日志）
     * @throws Exception 如果所有重试都失败
     */
    public static void executeWithRetry(Runnable operation, int maxRetries, long retryDelay, String operationName) throws Exception {
        executeWithRetry(() -> {
            operation.run();
            return null;
        }, maxRetries, retryDelay, operationName);
    }

    /**
     * 执行带重试的操作（使用默认参数）
     * 
     * @param operation 要执行的操作
     * @param operationName 操作名称
     * @param <T> 返回类型
     * @return 操作结果
     * @throws Exception 如果所有重试都失败
     */
    public static <T> T executeWithRetry(Supplier<T> operation, String operationName) throws Exception {
        return executeWithRetry(operation, 3, 1000, operationName);
    }

    /**
     * 执行带重试的操作（无返回值，使用默认参数）
     * 
     * @param operation 要执行的操作
     * @param operationName 操作名称
     * @throws Exception 如果所有重试都失败
     */
    public static void executeWithRetry(Runnable operation, String operationName) throws Exception {
        executeWithRetry(operation, 3, 1000, operationName);
    }
}