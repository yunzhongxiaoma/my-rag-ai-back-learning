package com.kinghy.rag.task;

import com.kinghy.rag.service.ChatCleanupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 聊天数据清理定时任务
 * 
 * @author yunzhongxiaoma
 */
@Component
@Slf4j
@ConditionalOnProperty(name = "chat.cleanup.enabled", havingValue = "true", matchIfMissing = true)
public class ChatCleanupTask {

    @Autowired
    private ChatCleanupService chatCleanupService;

    /**
     * 每天凌晨2点执行清理任务
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void dailyCleanup() {
        log.info("Starting daily chat cleanup task");
        
        try {
            // 清理7天前的非活跃会话
            int inactiveSessionsCleanup = chatCleanupService.cleanupInactiveSessions(7);
            log.info("Daily cleanup - cleaned up {} inactive sessions", inactiveSessionsCleanup);
            
        } catch (Exception e) {
            log.error("Daily cleanup task failed", e);
        }
    }

    /**
     * 每周日凌晨3点执行完整清理任务
     */
    @Scheduled(cron = "0 0 3 ? * SUN")
    public void weeklyCleanup() {
        log.info("Starting weekly chat cleanup task");
        
        try {
            chatCleanupService.performFullCleanup();
            
        } catch (Exception e) {
            log.error("Weekly cleanup task failed", e);
        }
    }

    /**
     * 每小时输出清理统计信息（仅在DEBUG级别）
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    public void logCleanupStats() {
        if (log.isDebugEnabled()) {
            try {
                String stats = chatCleanupService.getCleanupStats();
                log.debug("Chat cleanup statistics:\n{}", stats);
                
            } catch (Exception e) {
                log.debug("Failed to get cleanup statistics", e);
            }
        }
    }
}