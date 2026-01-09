package com.kinghy.rag.service.impl;

import com.kinghy.rag.mapper.ChatMessageMapper;
import com.kinghy.rag.mapper.ChatSessionMapper;
import com.kinghy.rag.service.ChatCacheService;
import com.kinghy.rag.service.ChatCleanupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 聊天数据清理服务实现类
 * 
 * @author yunzhongxiaoma
 */
@Service
@Slf4j
public class ChatCleanupServiceImpl implements ChatCleanupService {

    @Autowired
    private ChatSessionMapper chatSessionMapper;

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatCacheService chatCacheService;

    @Override
    @Transactional
    public int cleanupInactiveSessions(int inactiveDays) {
        log.info("Starting cleanup of inactive sessions older than {} days", inactiveDays);
        
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(inactiveDays);
            
            // 获取要清理的会话数量（用于统计）
            long count = chatSessionMapper.selectByCondition(null, 0, null, null, expireTime).size();
            
            // 清理过期的非活跃会话
            chatSessionMapper.cleanupInactiveSessions(expireTime);
            
            log.info("Successfully cleaned up {} inactive sessions", count);
            return (int) count;
            
        } catch (Exception e) {
            log.error("Failed to cleanup inactive sessions", e);
            return 0;
        }
    }

    @Override
    @Transactional
    public int cleanupExpiredMessages(int retentionDays) {
        log.info("Starting cleanup of expired messages older than {} days", retentionDays);
        
        try {
            LocalDateTime expireTime = LocalDateTime.now().minusDays(retentionDays);
            
            // 清理过期消息
            chatMessageMapper.cleanupExpiredMessages(expireTime);
            
            log.info("Successfully cleaned up expired messages older than {}", expireTime);
            return 1; // 返回1表示操作成功，具体数量由数据库处理
            
        } catch (Exception e) {
            log.error("Failed to cleanup expired messages", e);
            return 0;
        }
    }

    @Override
    @Transactional
    public void cleanupUserData(Integer userId) {
        log.info("Starting cleanup of all data for user: {}", userId);
        
        try {
            // 删除用户的所有消息
            chatMessageMapper.deleteUserMessages(userId);
            
            // 删除用户的所有会话
            chatSessionMapper.deleteUserSessions(userId);
            
            // 清除用户相关缓存
            chatCacheService.clearUserCache(userId);
            
            log.info("Successfully cleaned up all data for user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to cleanup user data for user: {}", userId, e);
            throw new RuntimeException("Failed to cleanup user data", e);
        }
    }

    @Override
    @Transactional
    public int archiveOldSessions(int archiveDays) {
        log.info("Starting archival of old sessions older than {} days", archiveDays);
        
        try {
            LocalDateTime archiveTime = LocalDateTime.now().minusDays(archiveDays);
            
            // 将旧会话标记为已结束状态（归档）
            // 这里可以扩展为将数据移动到归档表
            int count = chatSessionMapper.selectByCondition(null, 1, null, null, archiveTime).size();
            
            // 更新会话状态为已结束
            chatSessionMapper.selectByCondition(null, 1, null, null, archiveTime)
                    .forEach(session -> {
                        try {
                            chatSessionMapper.endSession(session.getSessionId(), session.getUserId());
                        } catch (Exception e) {
                            log.warn("Failed to archive session: {}", session.getSessionId(), e);
                        }
                    });
            
            log.info("Successfully archived {} old sessions", count);
            return count;
            
        } catch (Exception e) {
            log.error("Failed to archive old sessions", e);
            return 0;
        }
    }

    @Override
    public String getCleanupStats() {
        try {
            StringBuilder stats = new StringBuilder();
            stats.append("Chat Cleanup Statistics:\n");
            
            // 统计活跃会话数量
            long activeSessions = chatSessionMapper.selectByCondition(null, 1, null, null, null).size();
            stats.append("Active Sessions: ").append(activeSessions).append("\n");
            
            // 统计非活跃会话数量
            long inactiveSessions = chatSessionMapper.selectByCondition(null, 0, null, null, null).size();
            stats.append("Inactive Sessions: ").append(inactiveSessions).append("\n");
            
            // 统计最近7天的会话
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            long recentSessions = chatSessionMapper.selectByCondition(null, null, null, weekAgo, null).size();
            stats.append("Sessions (Last 7 days): ").append(recentSessions).append("\n");
            
            // 缓存统计
            stats.append("\n").append(chatCacheService.getCacheStats());
            
            return stats.toString();
            
        } catch (Exception e) {
            log.error("Failed to get cleanup statistics", e);
            return "Failed to get cleanup statistics: " + e.getMessage();
        }
    }

    @Override
    @Async
    public void performFullCleanup() {
        log.info("Starting full cleanup task");
        
        try {
            // 清理30天前的非活跃会话
            int inactiveSessionsCleanup = cleanupInactiveSessions(30);
            
            // 清理90天前的消息
            int expiredMessagesCleanup = cleanupExpiredMessages(90);
            
            // 归档60天前的会话
            int archivedSessions = archiveOldSessions(60);
            
            log.info("Full cleanup completed - Inactive sessions: {}, Expired messages: {}, Archived sessions: {}", 
                    inactiveSessionsCleanup, expiredMessagesCleanup, archivedSessions);
            
        } catch (Exception e) {
            log.error("Full cleanup task failed", e);
        }
    }
}