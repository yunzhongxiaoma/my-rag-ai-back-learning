package com.kinghy.rag.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinghy.rag.config.ChatPaginationConfig;
import com.kinghy.rag.entity.ChatSession;
import com.kinghy.rag.exception.SessionNotFoundException;
import com.kinghy.rag.mapper.ChatSessionMapper;
import com.kinghy.rag.pojo.vo.CursorPageResult;
import com.kinghy.rag.pojo.vo.PageResult;
import com.kinghy.rag.service.ChatCacheService;
import com.kinghy.rag.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 聊天会话服务实现类
 * 
 * @author yunzhongxiaoma
 * @description 针对表【chat_session】的数据库操作Service实现
 */
@Service
@Slf4j
public class ChatSessionServiceImpl extends ServiceImpl<ChatSessionMapper, ChatSession>
        implements ChatSessionService {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatCacheService chatCacheService;

    @Autowired
    private ChatPaginationConfig paginationConfig;

    @Override
    @Transactional
    public ChatSession createNewSession(Integer userId) {
        log.info("Creating new chat session for user: {}", userId);
        
        // 生成唯一的会话ID
        String sessionId = UUID.randomUUID().toString().replace("-", "");
        
        // 创建新会话
        ChatSession chatSession = ChatSession.builder()
                .sessionId(sessionId)
                .userId(userId)
                .title("新对话") // 默认标题，后续可根据首条消息更新
                .status(1) // 1表示活跃状态
                .messageCount(0)
                .lastMessageTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        // 将用户的其他会话设为非活跃状态
        chatSessionMapper.deactivateOtherSessions(userId, sessionId);
        
        // 保存新会话
        chatSessionMapper.insert(chatSession);
        
        // 更新缓存
        chatCacheService.cacheCurrentSession(userId, sessionId);
        chatCacheService.cacheSessionInfo(chatSession);
        chatCacheService.clearUserSessionsCache(userId); // 清除会话列表缓存，下次查询时重新加载
        
        log.info("Created new chat session with ID: {} for user: {}", sessionId, userId);
        return chatSession;
    }

    @Override
    public ChatSession getCurrentSession(Integer userId) {
        log.debug("Getting current session for user: {}", userId);
        
        // 先从缓存获取当前会话ID
        String cachedSessionId = chatCacheService.getCurrentSessionFromCache(userId);
        if (cachedSessionId != null) {
            // 从缓存获取会话信息
            ChatSession cachedSession = chatCacheService.getSessionInfoFromCache(cachedSessionId);
            if (cachedSession != null) {
                log.debug("Retrieved current session from cache for user: {}", userId);
                return cachedSession;
            }
        }
        
        // 缓存未命中，从数据库查询
        ChatSession currentSession = chatSessionMapper.getCurrentSessionByUserId(userId);
        if (currentSession != null) {
            // 更新缓存
            chatCacheService.cacheCurrentSession(userId, currentSession.getSessionId());
            chatCacheService.cacheSessionInfo(currentSession);
        }
        
        return currentSession;
    }

    @Override
    public ChatSession getSessionById(String sessionId, Integer userId) {
        log.debug("Getting session by ID: {} for user: {}", sessionId, userId);
        
        // 先从缓存获取
        ChatSession cachedSession = chatCacheService.getSessionInfoFromCache(sessionId);
        if (cachedSession != null && cachedSession.getUserId().equals(userId)) {
            log.debug("Retrieved session from cache: {}", sessionId);
            return cachedSession;
        }
        
        // 缓存未命中，从数据库查询
        ChatSession session = chatSessionMapper.getSessionByIdAndUserId(sessionId, userId);
        if (session != null) {
            // 更新缓存
            chatCacheService.cacheSessionInfo(session);
        }
        
        return session;
    }

    @Override
    public List<ChatSession> getUserSessions(Integer userId, int page, int size) {
        log.debug("Getting user sessions for user: {}, page: {}, size: {}", userId, page, size);
        
        // 验证并调整页面大小
        size = paginationConfig.validateSessionPageSize(size);
        
        // 对于第一页的请求，尝试从缓存获取
        if (page == 1 && size >= 20) {
            List<ChatSession> cachedSessions = chatCacheService.getUserSessionsFromCache(userId);
            if (cachedSessions != null && !cachedSessions.isEmpty()) {
                log.debug("Retrieved user sessions from cache for user: {}", userId);
                // 如果缓存的数据足够，直接返回所需的部分
                if (cachedSessions.size() >= size) {
                    return cachedSessions.subList(0, Math.min(size, cachedSessions.size()));
                }
            }
        }
        
        // 从数据库查询
        int offset = (page - 1) * size;
        List<ChatSession> sessions = chatSessionMapper.getUserSessionsPaged(userId, offset, size);
        
        // 如果是第一页且数据量合适，更新缓存
        if (page == 1 && sessions.size() <= 20) {
            chatCacheService.cacheUserSessions(userId, sessions);
        }
        
        return sessions;
    }

    @Override
    public PageResult<ChatSession> getUserSessionsWithTotal(Integer userId, int page, int size) {
        log.debug("Getting user sessions with total for user: {}, page: {}, size: {}", userId, page, size);
        
        // 验证并调整页面大小
        size = paginationConfig.validateSessionPageSize(size);
        
        // 获取总数
        long total = chatSessionMapper.getUserSessionCount(userId);
        
        if (total == 0) {
            return PageResult.empty(page, size);
        }
        
        // 获取数据
        List<ChatSession> sessions = getUserSessions(userId, page, size);
        
        return PageResult.of(sessions, total, page, size);
    }

    @Override
    public CursorPageResult<ChatSession> getUserSessionsByCursor(Integer userId, String cursor, int size) {
        log.debug("Getting user sessions by cursor for user: {}, cursor: {}, size: {}", userId, cursor, size);
        
        // 验证并调整页面大小
        size = paginationConfig.validateCursorPageSize(size);
        
        // 查询数据，多查询一条用于判断是否有下一页
        List<ChatSession> sessions = chatSessionMapper.getUserSessionsByCursor(userId, cursor, size + 1);
        
        boolean hasNext = sessions.size() > size;
        if (hasNext) {
            sessions = sessions.subList(0, size);
        }
        
        String nextCursor = null;
        if (hasNext && !sessions.isEmpty()) {
            nextCursor = sessions.get(sessions.size() - 1).getSessionId();
        }
        
        return CursorPageResult.of(sessions, cursor, nextCursor, size, hasNext);
    }

    @Override
    @Transactional
    public void updateSessionTitle(String sessionId, Integer userId, String title) {
        log.info("Updating session title for session: {} and user: {} to: {}", sessionId, userId, title);
        
        // 验证会话属于该用户
        ChatSession session = chatSessionMapper.getSessionByIdAndUserId(sessionId, userId);
        if (session == null) {
            log.warn("Session not found or access denied: {} for user: {}", sessionId, userId);
            throw new SessionNotFoundException(sessionId, userId);
        }
        
        try {
            chatSessionMapper.updateSessionTitle(sessionId, title);
            
            // 更新缓存中的会话信息
            session.setTitle(title);
            chatCacheService.cacheSessionInfo(session);
            chatCacheService.clearUserSessionsCache(userId); // 清除会话列表缓存
            
            log.info("Successfully updated session title for session: {}", sessionId);
        } catch (Exception e) {
            log.error("Failed to update session title for session: {}", sessionId, e);
            throw new RuntimeException("Failed to update session title", e);
        }
    }

    @Override
    @Transactional
    public void endSession(String sessionId, Integer userId) {
        log.info("Ending session: {} for user: {}", sessionId, userId);
        chatSessionMapper.endSession(sessionId, userId);
    }

    @Override
    @Transactional
    public void deleteSession(String sessionId, Integer userId) {
        log.info("Deleting session: {} for user: {}", sessionId, userId);
        
        // 验证会话属于该用户
        ChatSession session = chatSessionMapper.getSessionByIdAndUserId(sessionId, userId);
        if (session == null) {
            log.warn("Session not found or access denied: {} for user: {}", sessionId, userId);
            throw new SessionNotFoundException(sessionId, userId);
        }
        
        try {
            // 删除会话（级联删除消息由数据库外键约束处理）
            chatSessionMapper.deleteById(session.getId());
            
            // 清除相关缓存
            chatCacheService.clearSessionCache(sessionId, userId);
            
            log.info("Successfully deleted session: {} for user: {}", sessionId, userId);
        } catch (Exception e) {
            log.error("Failed to delete session: {} for user: {}", sessionId, userId, e);
            throw new RuntimeException("Failed to delete session", e);
        }
    }

    @Override
    @Transactional
    public void incrementMessageCount(String sessionId) {
        log.debug("Incrementing message count for session: {}", sessionId);
        chatSessionMapper.incrementMessageCount(sessionId);
    }

    @Override
    @Transactional
    public void updateLastMessageTime(String sessionId) {
        log.debug("Updating last message time for session: {}", sessionId);
        chatSessionMapper.updateLastMessageTime(sessionId);
    }

    @Override
    @Transactional
    public void activateSession(String sessionId, Integer userId) {
        log.info("Activating session: {} for user: {}", sessionId, userId);
        
        // 验证会话属于该用户
        ChatSession session = chatSessionMapper.getSessionByIdAndUserId(sessionId, userId);
        if (session == null) {
            log.warn("Session not found or access denied: {} for user: {}", sessionId, userId);
            throw new SessionNotFoundException(sessionId, userId);
        }
        
        try {
            // 将用户的其他会话设为非活跃状态
            chatSessionMapper.deactivateOtherSessions(userId, sessionId);
            
            // 激活指定会话
            chatSessionMapper.activateSession(sessionId, userId);
            
            // 更新缓存
            chatCacheService.cacheCurrentSession(userId, sessionId);
            chatCacheService.cacheSessionInfo(session);
            
            log.info("Successfully activated session: {} for user: {}", sessionId, userId);
        } catch (Exception e) {
            log.error("Failed to activate session: {} for user: {}", sessionId, userId, e);
            throw new RuntimeException("Failed to activate session", e);
        }
    }
}