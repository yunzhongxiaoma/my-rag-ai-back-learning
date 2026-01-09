package com.kinghy.rag.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 聊天消息实体类
 * 
 * @TableName chat_message
 */
@TableName(value = "chat_message")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    /**
     * 消息ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 用户ID
     */
    private Integer userId;

    /**
     * 消息类型：USER-用户消息，ASSISTANT-AI回复
     */
    private MessageType messageType;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息元数据（如引用文档、处理时间等）
     */
    private String metadata;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        USER, ASSISTANT
    }
}