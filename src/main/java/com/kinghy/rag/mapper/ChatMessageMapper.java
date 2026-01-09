package com.kinghy.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kinghy.rag.entity.ChatMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 聊天消息数据访问层
 * 
 * @author yunzhongxiaoma
 * @description 针对表【chat_message】的数据库操作Mapper
 */
@Mapper
public interface ChatMessageMapper extends BaseMapper<ChatMessage> {

    /**
     * 获取会话的所有消息（按时间顺序）
     */
    List<ChatMessage> getSessionMessages(@Param("sessionId") String sessionId, @Param("userId") Integer userId);

    /**
     * 获取会话消息（分页，按时间顺序）
     */
    List<ChatMessage> getSessionMessagesPaged(@Param("sessionId") String sessionId, 
                                            @Param("userId") Integer userId, 
                                            @Param("offset") int offset, 
                                            @Param("size") int size);

    /**
     * 获取会话最近的消息（按时间倒序）
     */
    List<ChatMessage> getRecentMessages(@Param("sessionId") String sessionId, 
                                      @Param("userId") Integer userId, 
                                      @Param("limit") int limit);

    /**
     * 删除会话的所有消息
     */
    void deleteSessionMessages(@Param("sessionId") String sessionId, @Param("userId") Integer userId);

    /**
     * 获取会话消息数量
     */
    long getMessageCount(@Param("sessionId") String sessionId, @Param("userId") Integer userId);

    /**
     * 获取用户在会话中的最后一条消息
     */
    ChatMessage getLastMessage(@Param("sessionId") String sessionId, @Param("userId") Integer userId);

    /**
     * 获取会话中的第一条用户消息（用于生成会话标题）
     */
    ChatMessage getFirstUserMessage(@Param("sessionId") String sessionId);

    /**
     * 根据用户ID获取所有消息（用于数据导出等场景）
     */
    List<ChatMessage> getAllUserMessages(@Param("userId") Integer userId, 
                                       @Param("offset") int offset, 
                                       @Param("size") int size);

    /**
     * 批量插入消息
     */
    void batchInsert(@Param("list") List<ChatMessage> messages);

    /**
     * 根据条件查询消息
     */
    List<ChatMessage> selectByCondition(@Param("sessionId") String sessionId,
                                      @Param("userId") Integer userId,
                                      @Param("messageType") ChatMessage.MessageType messageType,
                                      @Param("content") String content,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 获取用户消息统计
     */
    Map<String, Object> getUserMessageStats(@Param("userId") Integer userId, @Param("sessionId") String sessionId);

    /**
     * 删除用户的所有消息
     */
    void deleteUserMessages(@Param("userId") Integer userId);

    /**
     * 清理过期消息
     */
    void cleanupExpiredMessages(@Param("expireTime") LocalDateTime expireTime);

    /**
     * 获取会话消息摘要（用于会话列表显示）
     */
    Map<String, Object> getSessionMessageSummary(@Param("sessionId") String sessionId);

    /**
     * 获取会话消息（游标分页）
     */
    List<ChatMessage> getSessionMessagesByCursor(@Param("sessionId") String sessionId, 
                                               @Param("userId") Integer userId, 
                                               @Param("cursor") String cursor, 
                                               @Param("size") int size);

    /**
     * 搜索消息内容
     */
    List<ChatMessage> searchMessages(@Param("userId") Integer userId,
                                   @Param("keyword") String keyword,
                                   @Param("sessionId") String sessionId,
                                   @Param("offset") int offset,
                                   @Param("size") int size);
}