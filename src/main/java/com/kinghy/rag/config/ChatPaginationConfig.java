package com.kinghy.rag.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 聊天分页配置
 * 
 * @author yunzhongxiaoma
 * @description 聊天相关分页参数配置
 */
@Configuration
@ConfigurationProperties(prefix = "chat.pagination")
@Data
public class ChatPaginationConfig {

    /**
     * 默认消息页面大小
     */
    private int defaultMessagePageSize = 20;

    /**
     * 最大消息页面大小
     */
    private int maxMessagePageSize = 100;

    /**
     * 默认会话页面大小
     */
    private int defaultSessionPageSize = 20;

    /**
     * 最大会话页面大小
     */
    private int maxSessionPageSize = 50;

    /**
     * 最近消息默认限制数量
     */
    private int defaultRecentMessageLimit = 50;

    /**
     * 最近消息最大限制数量
     */
    private int maxRecentMessageLimit = 200;

    /**
     * 会话预览消息数量
     */
    private int sessionPreviewMessageCount = 3;

    /**
     * 是否启用游标分页
     */
    private boolean enableCursorPagination = true;

    /**
     * 游标分页默认大小
     */
    private int defaultCursorPageSize = 20;

    /**
     * 游标分页最大大小
     */
    private int maxCursorPageSize = 100;

    /**
     * 验证并调整页面大小
     * 
     * @param requestedSize 请求的页面大小
     * @param defaultSize 默认大小
     * @param maxSize 最大大小
     * @return 调整后的页面大小
     */
    public int validatePageSize(int requestedSize, int defaultSize, int maxSize) {
        if (requestedSize <= 0) {
            return defaultSize;
        }
        return Math.min(requestedSize, maxSize);
    }

    /**
     * 验证消息页面大小
     */
    public int validateMessagePageSize(int requestedSize) {
        return validatePageSize(requestedSize, defaultMessagePageSize, maxMessagePageSize);
    }

    /**
     * 验证会话页面大小
     */
    public int validateSessionPageSize(int requestedSize) {
        return validatePageSize(requestedSize, defaultSessionPageSize, maxSessionPageSize);
    }

    /**
     * 验证最近消息限制数量
     */
    public int validateRecentMessageLimit(int requestedLimit) {
        return validatePageSize(requestedLimit, defaultRecentMessageLimit, maxRecentMessageLimit);
    }

    /**
     * 验证游标分页大小
     */
    public int validateCursorPageSize(int requestedSize) {
        return validatePageSize(requestedSize, defaultCursorPageSize, maxCursorPageSize);
    }
}