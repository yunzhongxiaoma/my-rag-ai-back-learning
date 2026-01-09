package com.kinghy.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kinghy.rag.entity.ChatSession;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话数据访问层
 * 
 * @author yunzhongxiaoma
 * @description 针对表【chat_session】的数据库操作Mapper
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * 根据用户ID获取当前活跃会话
     */
    ChatSession getCurrentSessionByUserId(@Param("userId") Integer userId);

    /**
     * 根据会话ID和用户ID获取会话
     */
    ChatSession getSessionByIdAndUserId(@Param("sessionId") String sessionId, @Param("userId") Integer userId);

    /**
     * 获取用户会话列表（分页）
     */
    List<ChatSession> getUserSessionsPaged(@Param("userId") Integer userId, @Param("offset") int offset, @Param("size") int size);

    /**
     * 获取用户会话（游标分页）
     */
    List<ChatSession> getUserSessionsByCursor(@Param("userId") Integer userId, 
                                            @Param("cursor") String cursor, 
                                            @Param("size") int size);

    /**
     * 获取用户会话总数
     */
    long getUserSessionCount(@Param("userId") Integer userId);

    /**
     * 更新会话标题
     */
    void updateSessionTitle(@Param("sessionId") String sessionId, @Param("title") String title);

    /**
     * 结束会话
     */
    void endSession(@Param("sessionId") String sessionId, @Param("userId") Integer userId);

    /**
     * 增加消息计数
     */
    void incrementMessageCount(@Param("sessionId") String sessionId);

    /**
     * 更新最后消息时间
     */
    void updateLastMessageTime(@Param("sessionId") String sessionId);

    /**
     * 将用户的其他会话设为非活跃状态
     */
    void deactivateOtherSessions(@Param("userId") Integer userId, @Param("sessionId") String sessionId);

    /**
     * 激活指定会话
     */
    void activateSession(@Param("sessionId") String sessionId, @Param("userId") Integer userId);

    /**
     * 批量插入会话
     */
    void batchInsert(@Param("list") List<ChatSession> sessions);

    /**
     * 根据条件查询会话
     */
    List<ChatSession> selectByCondition(@Param("userId") Integer userId,
                                      @Param("status") Integer status,
                                      @Param("title") String title,
                                      @Param("startTime") LocalDateTime startTime,
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 删除用户的所有会话
     */
    void deleteUserSessions(@Param("userId") Integer userId);

    /**
     * 清理过期的非活跃会话
     */
    void cleanupInactiveSessions(@Param("expireTime") LocalDateTime expireTime);
}