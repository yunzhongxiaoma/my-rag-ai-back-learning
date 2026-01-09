package com.kinghy.rag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinghy.rag.entity.ChatSession;
import com.kinghy.rag.pojo.vo.CursorPageResult;
import com.kinghy.rag.pojo.vo.PageResult;

import java.util.List;

/**
 * 聊天会话服务接口
 * 
 * @author yunzhongxiaoma
 * @description 针对表【chat_session】的数据库操作Service
 */
public interface ChatSessionService extends IService<ChatSession> {

    /**
     * 为用户创建新的聊天会话
     * 
     * @param userId 用户ID
     * @return 创建的会话对象
     */
    ChatSession createNewSession(Integer userId);

    /**
     * 获取用户当前活跃的会话
     * 
     * @param userId 用户ID
     * @return 当前活跃会话，如果没有则返回null
     */
    ChatSession getCurrentSession(Integer userId);

    /**
     * 根据会话ID获取指定会话（需验证用户权限）
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 会话对象，如果不存在或无权限则返回null
     */
    ChatSession getSessionById(String sessionId, Integer userId);

    /**
     * 获取用户的会话列表（分页）
     * 
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 会话列表
     */
    List<ChatSession> getUserSessions(Integer userId, int page, int size);

    /**
     * 获取用户的会话列表（分页，带总数）
     * 
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<ChatSession> getUserSessionsWithTotal(Integer userId, int page, int size);

    /**
     * 获取用户的会话列表（游标分页）
     * 
     * @param userId 用户ID
     * @param cursor 游标（会话ID或时间戳）
     * @param size 每页大小
     * @return 游标分页结果
     */
    CursorPageResult<ChatSession> getUserSessionsByCursor(Integer userId, String cursor, int size);

    /**
     * 更新会话标题
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param title 新标题
     */
    void updateSessionTitle(String sessionId, Integer userId, String title);

    /**
     * 结束会话
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    void endSession(String sessionId, Integer userId);

    /**
     * 删除会话及其所有消息
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    void deleteSession(String sessionId, Integer userId);

    /**
     * 增加会话消息计数
     * 
     * @param sessionId 会话ID
     */
    void incrementMessageCount(String sessionId);

    /**
     * 更新会话最后消息时间
     * 
     * @param sessionId 会话ID
     */
    void updateLastMessageTime(String sessionId);

    /**
     * 激活会话（设置为当前活跃会话）
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    void activateSession(String sessionId, Integer userId);
}