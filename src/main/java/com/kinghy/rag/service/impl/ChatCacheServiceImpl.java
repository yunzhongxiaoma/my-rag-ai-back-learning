package com.kinghy.rag.service.impl;

import com.kinghy.rag.entity.ChatMessage;
import com.kinghy.rag.entity.ChatSession;
import com.kinghy.rag.service.ChatCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天缓存服务实现类
 * 
 * @author yunzhongxiaoma
 * @description 提供聊天相关数据的缓存管理功能实现
 */
@Service
@Slf4j
public class ChatCacheServiceImpl implements ChatCacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 缓存键前缀
    private static final String CURRENT_SESSION_PREFIX = "chat:session:current:";
    private static final String SESSION_INFO_PREFIX = "chat:session:info:";
    private static final String USER_SESSIONS_PREFIX = "chat:user:sessions:";
    private static final String RECENT_MESSAGES_PREFIX = "chat:messages:recent:";

    // 缓存过期时间
    private static final Duration CURRENT_SESSION_TTL = Duration.ofHours(4);
    private static final Duration SESSION_INFO_TTL = Duration.ofHours(2);
    private static final Duration USER_SESSIONS_TTL = Duration.ofMinutes(30);
    private static final Duration RECENT_MESSAGES_TTL = Duration.ofHours(1);

    // ==================== Session Cache ====================

    @Override
    public void cacheCurrentSession(Integer userId, String sessionId) {
        String key = CURRENT_SESSION_PREFIX + userId;
        try {
            redisTemplate.opsForValue().set(key, sessionId, CURRENT_SESSION_TTL);
            log.debug("缓存用户当前会话，用户ID: {}, 会话ID: {}", userId, sessionId);
        } catch (Exception e) {
            log.error("缓存用户当前会话失败，用户ID: {}, 会话ID: {}", userId, sessionId, e);
        }
    }

    @Override
    public String getCurrentSessionFromCache(Integer userId) {
        String key = CURRENT_SESSION_PREFIX + userId;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null) {
                log.debug("从缓存获取用户当前会话，用户ID: {}, 会话ID: {}", userId, value);
                return value.toString();
            }
        } catch (Exception e) {
            log.error("从缓存获取用户当前会话失败，用户ID: {}", userId, e);
        }
        return null;
    }

    @Override
    public void cacheSessionInfo(ChatSession session) {
        String key = SESSION_INFO_PREFIX + session.getSessionId();
        try {
            redisTemplate.opsForValue().set(key, session, SESSION_INFO_TTL);
            log.debug("缓存会话信息，会话ID: {}", session.getSessionId());
        } catch (Exception e) {
            log.error("缓存会话信息失败，会话ID: {}", session.getSessionId(), e);
        }
    }

    @Override
    public ChatSession getSessionInfoFromCache(String sessionId) {
        String key = SESSION_INFO_PREFIX + sessionId;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof ChatSession) {
                log.debug("从缓存获取会话信息，会话ID: {}", sessionId);
                return (ChatSession) value;
            }
        } catch (Exception e) {
            log.error("从缓存获取会话信息失败，会话ID: {}", sessionId, e);
        }
        return null;
    }

    @Override
    public void cacheUserSessions(Integer userId, List<ChatSession> sessions) {
        String key = USER_SESSIONS_PREFIX + userId;
        try {
            redisTemplate.opsForValue().set(key, sessions, USER_SESSIONS_TTL);
            log.debug("缓存用户会话列表，用户ID: {}, 会话数量: {}", userId, sessions.size());
        } catch (Exception e) {
            log.error("缓存用户会话列表失败，用户ID: {}", userId, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ChatSession> getUserSessionsFromCache(Integer userId) {
        String key = USER_SESSIONS_PREFIX + userId;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof List) {
                log.debug("从缓存获取用户会话列表，用户ID: {}", userId);
                return (List<ChatSession>) value;
            }
        } catch (Exception e) {
            log.error("从缓存获取用户会话列表失败，用户ID: {}", userId, e);
        }
        return null;
    }

    // ==================== Message Cache ====================

    @Override
    public void cacheRecentMessages(String sessionId, List<ChatMessage> messages) {
        String key = RECENT_MESSAGES_PREFIX + sessionId;
        try {
            // 只缓存最近50条消息
            List<ChatMessage> messagesToCache = messages.size() > 50 ? 
                    messages.subList(Math.max(0, messages.size() - 50), messages.size()) : messages;
            
            redisTemplate.opsForValue().set(key, messagesToCache, RECENT_MESSAGES_TTL);
            log.debug("缓存会话最近消息，会话ID: {}, 消息数量: {}", sessionId, messagesToCache.size());
        } catch (Exception e) {
            log.error("缓存会话最近消息失败，会话ID: {}", sessionId, e);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ChatMessage> getRecentMessagesFromCache(String sessionId) {
        String key = RECENT_MESSAGES_PREFIX + sessionId;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value instanceof List) {
                log.debug("从缓存获取会话最近消息，会话ID: {}", sessionId);
                return (List<ChatMessage>) value;
            }
        } catch (Exception e) {
            log.error("从缓存获取会话最近消息失败，会话ID: {}", sessionId, e);
        }
        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addMessageToCache(String sessionId, ChatMessage message) {
        String key = RECENT_MESSAGES_PREFIX + sessionId;
        try {
            Object value = redisTemplate.opsForValue().get(key);
            List<ChatMessage> messages = new ArrayList<>();
            
            if (value instanceof List) {
                messages = new ArrayList<>((List<ChatMessage>) value);
            }
            
            // 添加新消息
            messages.add(message);
            
            // 保持最多50条消息
            if (messages.size() > 50) {
                messages = messages.subList(messages.size() - 50, messages.size());
            }
            
            // 更新缓存
            redisTemplate.opsForValue().set(key, messages, RECENT_MESSAGES_TTL);
            log.debug("添加消息到缓存，会话ID: {}, 消息ID: {}", sessionId, message.getId());
        } catch (Exception e) {
            log.error("添加消息到缓存失败，会话ID: {}, 消息ID: {}", sessionId, message.getId(), e);
        }
    }

    // ==================== Cache Invalidation ====================

    @Override
    public void clearUserCache(Integer userId) {
        try {
            // 清除用户当前会话缓存
            redisTemplate.delete(CURRENT_SESSION_PREFIX + userId);
            
            // 清除用户会话列表缓存
            redisTemplate.delete(USER_SESSIONS_PREFIX + userId);
            
            log.info("清除用户缓存，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("清除用户缓存失败，用户ID: {}", userId, e);
        }
    }

    @Override
    public void clearSessionCache(String sessionId, Integer userId) {
        try {
            // 清除会话信息缓存
            redisTemplate.delete(SESSION_INFO_PREFIX + sessionId);
            
            // 清除会话消息缓存
            redisTemplate.delete(RECENT_MESSAGES_PREFIX + sessionId);
            
            // 清除用户会话列表缓存（因为会话可能被删除或修改）
            redisTemplate.delete(USER_SESSIONS_PREFIX + userId);
            
            log.info("清除会话缓存，会话ID: {}, 用户ID: {}", sessionId, userId);
        } catch (Exception e) {
            log.error("清除会话缓存失败，会话ID: {}, 用户ID: {}", sessionId, userId, e);
        }
    }

    @Override
    public void clearUserSessionsCache(Integer userId) {
        try {
            redisTemplate.delete(USER_SESSIONS_PREFIX + userId);
            log.debug("清除用户会话列表缓存，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("清除用户会话列表缓存失败，用户ID: {}", userId, e);
        }
    }

    @Override
    public void clearSessionMessagesCache(String sessionId) {
        try {
            redisTemplate.delete(RECENT_MESSAGES_PREFIX + sessionId);
            log.debug("清除会话消息缓存，会话ID: {}", sessionId);
        } catch (Exception e) {
            log.error("清除会话消息缓存失败，会话ID: {}", sessionId, e);
        }
    }

    // ==================== Cache Statistics ====================

    @Override
    public String getCacheStats() {
        try {
            StringBuilder stats = new StringBuilder();
            stats.append("Chat Cache Statistics:\n");
            
            // 统计各类缓存的数量
            long currentSessionCount = countKeys(CURRENT_SESSION_PREFIX + "*");
            long sessionInfoCount = countKeys(SESSION_INFO_PREFIX + "*");
            long userSessionsCount = countKeys(USER_SESSIONS_PREFIX + "*");
            long recentMessagesCount = countKeys(RECENT_MESSAGES_PREFIX + "*");
            
            stats.append("Current Sessions: ").append(currentSessionCount).append("\n");
            stats.append("Session Info: ").append(sessionInfoCount).append("\n");
            stats.append("User Sessions Lists: ").append(userSessionsCount).append("\n");
            stats.append("Recent Messages: ").append(recentMessagesCount).append("\n");
            
            return stats.toString();
        } catch (Exception e) {
            log.error("获取缓存统计信息失败", e);
            return "Failed to get cache statistics: " + e.getMessage();
        }
    }

    @Override
    public void warmUpCache(Integer userId) {
        log.info("Cache warm-up for user {} should be called from service layer to avoid circular dependencies", userId);
        // This method should be called from the service layer that has access to both
        // the cache service and the business services, not implemented here to avoid circular dependencies
    }

    /**
     * 统计匹配模式的键数量
     */
    private long countKeys(String pattern) {
        try {
            return redisTemplate.keys(pattern).size();
        } catch (Exception e) {
            log.error("统计键数量失败，模式: {}", pattern, e);
            return 0;
        }
    }
}