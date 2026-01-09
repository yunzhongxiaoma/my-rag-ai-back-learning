package com.kinghy.rag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinghy.rag.entity.ChatMessage;
import com.kinghy.rag.pojo.vo.CursorPageResult;
import com.kinghy.rag.pojo.vo.PageResult;

import java.util.List;

/**
 * 聊天消息服务接口
 * 
 * @author yunzhongxiaoma
 * @description 针对表【chat_message】的数据库操作Service
 */
public interface ChatMessageService extends IService<ChatMessage> {

    /**
     * 保存用户消息
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param content 消息内容
     * @return 保存的消息对象
     */
    ChatMessage saveUserMessage(String sessionId, Integer userId, String content);

    /**
     * 保存AI助手消息
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param content 消息内容
     * @param metadata 消息元数据（JSON格式）
     * @return 保存的消息对象
     */
    ChatMessage saveAssistantMessage(String sessionId, Integer userId, String content, String metadata);

    /**
     * 获取会话的所有消息
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 消息列表（按时间顺序）
     */
    List<ChatMessage> getSessionMessages(String sessionId, Integer userId);

    /**
     * 获取会话消息（分页）
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 消息列表（按时间顺序）
     */
    List<ChatMessage> getSessionMessages(String sessionId, Integer userId, int page, int size);

    /**
     * 获取会话消息（分页，带总数）
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param page 页码（从1开始）
     * @param size 每页大小
     * @return 分页结果
     */
    PageResult<ChatMessage> getSessionMessagesWithTotal(String sessionId, Integer userId, int page, int size);

    /**
     * 获取会话消息（游标分页）
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param cursor 游标（消息ID或时间戳）
     * @param size 每页大小
     * @return 游标分页结果
     */
    CursorPageResult<ChatMessage> getSessionMessagesByCursor(String sessionId, Integer userId, String cursor, int size);

    /**
     * 获取会话最近的消息
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @param limit 消息数量限制
     * @return 最近的消息列表（按时间倒序）
     */
    List<ChatMessage> getRecentMessages(String sessionId, Integer userId, int limit);

    /**
     * 删除会话的所有消息
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     */
    void deleteSessionMessages(String sessionId, Integer userId);

    /**
     * 获取会话消息数量
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 消息数量
     */
    long getMessageCount(String sessionId, Integer userId);

    /**
     * 获取用户在会话中的最后一条消息
     * 
     * @param sessionId 会话ID
     * @param userId 用户ID
     * @return 最后一条消息，如果没有则返回null
     */
    ChatMessage getLastMessage(String sessionId, Integer userId);

    /**
     * 批量保存消息
     * 
     * @param messages 消息列表
     * @return 是否保存成功
     */
    boolean saveBatch(List<ChatMessage> messages);
}