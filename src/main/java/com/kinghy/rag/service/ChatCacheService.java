package com.kinghy.rag.service;

import com.kinghy.rag.entity.ChatMessage;
import com.kinghy.rag.entity.ChatSession;

import java.util.List;

/**
 * 聊天缓存服务接口
 * 
 * @author yunzhongxiaoma
 * @description 提供聊天相关数据的缓存管理功能
 */
public interface ChatCacheService {

    // ==================== Session Cache ====================
    
    /**
     * 缓存用户当前会话ID
     * 
     * @param userId 用户ID
     * @param sessionId 会话ID
     */
    void cacheCurrentSession(Integer userId, String sessionId);

    /**
     * 获取用户当前会话ID
     * 
     * @param userId 用户ID
     * @return 当前会话ID，如果不存在则返回null
     */
    String getCurrentSessionFromCache(Integer userId);

    /**
     * 缓存会话信息
     * 
     * @param session 会话对象
     */
    void cacheSessionInfo(ChatSession session);

    /**
     * 从缓存获取会话信息
     * 
     * @param sessionId 会话ID
     * @return 会话对象，如果不存在则返回null
     */
    ChatSession getSessionInfoFromCache(String sessionId);

    /**
     * 缓存用户会话列表
     * 
     * @param userId 用户ID
     * @param sessions 会话列表
     */
    void cacheUserSessions(Integer userId, List<ChatSession> sessions);

    /**
     * 从缓存获取用户会话列表
     * 
     * @param userId 用户ID
     * @return 会话列表，如果不存在则返回null
     */
    List<ChatSession> getUserSessionsFromCache(Integer userId);

    // ==================== Message Cache ====================
    
    /**
     * 缓存会话的最近消息
     * 
     * @param sessionId 会话ID
     * @param messages 消息列表
     */
    void cacheRecentMessages(String sessionId, List<ChatMessage> messages);

    /**
     * 从缓存获取会话的最近消息
     * 
     * @param sessionId 会话ID
     * @return 消息列表，如果不存在则返回null
     */
    List<ChatMessage> getRecentMessagesFromCache(String sessionId);

    /**
     * 添加新消息到缓存
     * 
     * @param sessionId 会话ID
     * @param message 新消息
     */
    void addMessageToCache(String sessionId, ChatMessage message);

    // ==================== Cache Invalidation ====================
    
    /**
     * 清除用户相关的所有缓存
     * 
     * @param userId 用户ID
     */
    void clearUserCache(Integer userId);

    /**
     * 清除会话相关的所有缓存
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    void clearSessionCache(String sessionId, Integer userId);

    /**
     * 清除用户会话列表缓存
     * 
     * @param userId 用户ID
     */
    void clearUserSessionsCache(Integer userId);

    /**
     * 清除会话消息缓存
     * 
     * @param sessionId 会话ID
     */
    void clearSessionMessagesCache(String sessionId);

    // ==================== Cache Statistics ====================
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计信息
     */
    String getCacheStats();

    /**
     * 预热缓存
     * 
     * @param userId 用户ID
     */
    void warmUpCache(Integer userId);
}