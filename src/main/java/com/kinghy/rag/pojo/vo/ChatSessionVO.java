package com.kinghy.rag.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话视图对象
 * 
 * @author yunzhongxiaoma
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionVO {
    
    /**
     * 会话唯一标识符
     */
    private String sessionId;
    
    /**
     * 会话标题
     */
    private String title;
    
    /**
     * 会话状态：1-活跃，0-已结束
     */
    private Integer status;
    
    /**
     * 消息数量
     */
    private Integer messageCount;
    
    /**
     * 最后消息时间
     */
    private LocalDateTime lastMessageTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 最近消息预览（用于会话列表显示）
     */
    private List<ChatMessageVO> recentMessages;
}