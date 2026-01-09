package com.kinghy.rag.service;

/**
 * 聊天数据清理服务接口
 * 
 * @author yunzhongxiaoma
 */
public interface ChatCleanupService {

    /**
     * 清理过期的非活跃会话
     * 
     * @param inactiveDays 非活跃天数阈值
     * @return 清理的会话数量
     */
    int cleanupInactiveSessions(int inactiveDays);

    /**
     * 清理过期的消息
     * 
     * @param retentionDays 消息保留天数
     * @return 清理的消息数量
     */
    int cleanupExpiredMessages(int retentionDays);

    /**
     * 清理用户的所有数据
     * 
     * @param userId 用户ID
     */
    void cleanupUserData(Integer userId);

    /**
     * 归档旧会话
     * 
     * @param archiveDays 归档天数阈值
     * @return 归档的会话数量
     */
    int archiveOldSessions(int archiveDays);

    /**
     * 获取清理统计信息
     * 
     * @return 清理统计信息
     */
    String getCleanupStats();

    /**
     * 执行完整的数据清理任务
     */
    void performFullCleanup();
}