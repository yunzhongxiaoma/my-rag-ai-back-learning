package com.kinghy.rag.pojo.vo;

import com.kinghy.rag.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 聊天消息视图对象
 * 
 * @author yunzhongxiaoma
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 消息类型：USER-用户消息，ASSISTANT-AI回复
     */
    private ChatMessage.MessageType messageType;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息元数据（解析后的Map格式）
     */
    private Map<String, Object> metadata;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}