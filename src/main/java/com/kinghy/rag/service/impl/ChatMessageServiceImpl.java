package com.kinghy.rag.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kinghy.rag.config.ChatPaginationConfig;
import com.kinghy.rag.entity.ChatMessage;
import com.kinghy.rag.exception.MessagePersistenceException;
import com.kinghy.rag.mapper.ChatMessageMapper;
import com.kinghy.rag.pojo.vo.CursorPageResult;
import com.kinghy.rag.pojo.vo.PageResult;
import com.kinghy.rag.service.ChatCacheService;
import com.kinghy.rag.service.ChatMessageService;
import com.kinghy.rag.service.ChatSessionService;
import com.kinghy.rag.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天消息服务实现类
 * 
 * @author yunzhongxiaoma
 * @description 针对表【chat_message】的数据库操作Service实现
 */
@Service
@Slf4j
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage>
        implements ChatMessageService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatCacheService chatCacheService;

    @Autowired
    private ChatPaginationConfig paginationConfig;

    @Override
    @Transactional
    public ChatMessage saveUserMessage(String sessionId, Integer userId, String content) {
        log.info("Saving user message for session: {}, user: {}", sessionId, userId);
        
        try {
            ChatMessage message = ChatMessage.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .messageType(ChatMessage.MessageType.USER)
                    .content(content)
                    .metadata(null) // 用户消息通常不需要元数据
                    .createTime(LocalDateTime.now())
                    .build();
            
            // 使用重试机制保存消息
            ChatMessage savedMessage = RetryUtil.executeWithRetry(() -> {
                chatMessageMapper.insert(message);
                return message;
            }, 3, 500, "save user message");
            
            // 更新会话统计信息（允许失败，不影响消息保存）
            try {
                updateSessionStats(sessionId);
            } catch (Exception e) {
                log.warn("Failed to update session stats for session: {}, but message was saved successfully", sessionId, e);
            }
            
            // 添加到缓存（允许失败）
            try {
                chatCacheService.addMessageToCache(sessionId, savedMessage);
            } catch (Exception e) {
                log.warn("Failed to add message to cache for session: {}", sessionId, e);
            }
            
            log.info("Successfully saved user message with ID: {} for session: {}", savedMessage.getId(), sessionId);
            return savedMessage;
            
        } catch (Exception e) {
            log.error("Failed to save user message for session: {}, user: {}", sessionId, userId, e);
            throw new MessagePersistenceException(sessionId, "save user message", e);
        }
    }

    @Override
    @Transactional
    public ChatMessage saveAssistantMessage(String sessionId, Integer userId, String content, String metadata) {
        log.info("Saving assistant message for session: {}, user: {}", sessionId, userId);
        
        try {
            ChatMessage message = ChatMessage.builder()
                    .sessionId(sessionId)
                    .userId(userId)
                    .messageType(ChatMessage.MessageType.ASSISTANT)
                    .content(content)
                    .metadata(metadata)
                    .createTime(LocalDateTime.now())
                    .build();
            
            // 使用重试机制保存消息
            ChatMessage savedMessage = RetryUtil.executeWithRetry(() -> {
                chatMessageMapper.insert(message);
                return message;
            }, 3, 500, "save assistant message");
            
            // 更新会话统计信息（允许失败，不影响消息保存）
            try {
                updateSessionStats(sessionId);
            } catch (Exception e) {
                log.warn("Failed to update session stats for session: {}, but message was saved successfully", sessionId, e);
            }
            
            // 添加到缓存（允许失败）
            try {
                chatCacheService.addMessageToCache(sessionId, savedMessage);
            } catch (Exception e) {
                log.warn("Failed to add message to cache for session: {}", sessionId, e);
            }
            
            log.info("Successfully saved assistant message with ID: {} for session: {}", savedMessage.getId(), sessionId);
            return savedMessage;
            
        } catch (Exception e) {
            log.error("Failed to save assistant message for session: {}, user: {}", sessionId, userId, e);
            throw new MessagePersistenceException(sessionId, "save assistant message", e);
        }
    }

    @Override
    public List<ChatMessage> getSessionMessages(String sessionId, Integer userId) {
        log.debug("Getting all messages for session: {}, user: {}", sessionId, userId);
        return chatMessageMapper.getSessionMessages(sessionId, userId);
    }

    @Override
    public List<ChatMessage> getSessionMessages(String sessionId, Integer userId, int page, int size) {
        log.debug("Getting messages for session: {}, user: {}, page: {}, size: {}", sessionId, userId, page, size);
        
        // 验证并调整页面大小
        size = paginationConfig.validateMessagePageSize(size);
        
        int offset = (page - 1) * size;
        return chatMessageMapper.getSessionMessagesPaged(sessionId, userId, offset, size);
    }

    @Override
    public PageResult<ChatMessage> getSessionMessagesWithTotal(String sessionId, Integer userId, int page, int size) {
        log.debug("Getting messages with total for session: {}, user: {}, page: {}, size: {}", sessionId, userId, page, size);
        
        // 验证并调整页面大小
        size = paginationConfig.validateMessagePageSize(size);
        
        // 获取总数
        long total = chatMessageMapper.getMessageCount(sessionId, userId);
        
        if (total == 0) {
            return PageResult.empty(page, size);
        }
        
        // 获取数据
        List<ChatMessage> messages = getSessionMessages(sessionId, userId, page, size);
        
        return PageResult.of(messages, total, page, size);
    }

    @Override
    public CursorPageResult<ChatMessage> getSessionMessagesByCursor(String sessionId, Integer userId, String cursor, int size) {
        log.debug("Getting messages by cursor for session: {}, user: {}, cursor: {}, size: {}", sessionId, userId, cursor, size);
        
        // 验证并调整页面大小
        size = paginationConfig.validateCursorPageSize(size);
        
        // 查询数据，多查询一条用于判断是否有下一页
        List<ChatMessage> messages = chatMessageMapper.getSessionMessagesByCursor(sessionId, userId, cursor, size + 1);
        
        boolean hasNext = messages.size() > size;
        if (hasNext) {
            messages = messages.subList(0, size);
        }
        
        String nextCursor = null;
        if (hasNext && !messages.isEmpty()) {
            nextCursor = messages.get(messages.size() - 1).getId().toString();
        }
        
        return CursorPageResult.of(messages, cursor, nextCursor, size, hasNext);
    }

    @Override
    public List<ChatMessage> getRecentMessages(String sessionId, Integer userId, int limit) {
        log.debug("Getting recent messages for session: {}, user: {}, limit: {}", sessionId, userId, limit);
        
        // 验证并调整限制数量
        limit = paginationConfig.validateRecentMessageLimit(limit);
        
        // 先尝试从缓存获取
        List<ChatMessage> cachedMessages = chatCacheService.getRecentMessagesFromCache(sessionId);
        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            // 过滤用户权限并限制数量
            List<ChatMessage> filteredMessages = cachedMessages.stream()
                    .filter(msg -> msg.getUserId().equals(userId))
                    .sorted((m1, m2) -> m2.getCreateTime().compareTo(m1.getCreateTime())) // 按时间倒序
                    .limit(limit)
                    .toList();
            
            if (!filteredMessages.isEmpty()) {
                log.debug("Retrieved recent messages from cache for session: {}", sessionId);
                return filteredMessages;
            }
        }
        
        // 缓存未命中，从数据库查询
        List<ChatMessage> messages = chatMessageMapper.getRecentMessages(sessionId, userId, limit);
        
        // 更新缓存（如果查询的是较多的消息）
        if (limit >= 20 && !messages.isEmpty()) {
            chatCacheService.cacheRecentMessages(sessionId, messages);
        }
        
        return messages;
    }

    @Override
    @Transactional
    public void deleteSessionMessages(String sessionId, Integer userId) {
        log.info("Deleting all messages for session: {}, user: {}", sessionId, userId);
        chatMessageMapper.deleteSessionMessages(sessionId, userId);
        
        // 清除消息缓存
        chatCacheService.clearSessionMessagesCache(sessionId);
        
        // 重置会话消息计数
        chatSessionService.updateSessionTitle(sessionId, userId, "新对话");
        log.info("Successfully deleted all messages for session: {}", sessionId);
    }

    @Override
    public long getMessageCount(String sessionId, Integer userId) {
        log.debug("Getting message count for session: {}, user: {}", sessionId, userId);
        return chatMessageMapper.getMessageCount(sessionId, userId);
    }

    @Override
    public ChatMessage getLastMessage(String sessionId, Integer userId) {
        log.debug("Getting last message for session: {}, user: {}", sessionId, userId);
        return chatMessageMapper.getLastMessage(sessionId, userId);
    }

    @Override
    @Transactional
    public boolean saveBatch(List<ChatMessage> messages) {
        log.info("Batch saving {} messages", messages.size());
        
        if (messages == null || messages.isEmpty()) {
            log.warn("No messages to save in batch operation");
            return true;
        }
        
        try {
            // 设置创建时间
            LocalDateTime now = LocalDateTime.now();
            messages.forEach(message -> {
                if (message.getCreateTime() == null) {
                    message.setCreateTime(now);
                }
            });
            
            // 批量保存
            boolean result = super.saveBatch(messages);
            
            // 更新相关会话的统计信息
            messages.stream()
                    .map(ChatMessage::getSessionId)
                    .distinct()
                    .forEach(this::updateSessionStats);
            
            log.info("Successfully batch saved {} messages", messages.size());
            return result;
        } catch (Exception e) {
            log.error("Failed to batch save messages", e);
            return false;
        }
    }

    /**
     * 更新会话统计信息（消息计数和最后消息时间）
     * 
     * @param sessionId 会话ID
     */
    private void updateSessionStats(String sessionId) {
        try {
            chatSessionService.incrementMessageCount(sessionId);
            chatSessionService.updateLastMessageTime(sessionId);
        } catch (Exception e) {
            log.error("Failed to update session stats for session: {}", sessionId, e);
        }
    }
}