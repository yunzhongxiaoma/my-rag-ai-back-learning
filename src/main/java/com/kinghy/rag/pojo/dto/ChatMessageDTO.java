package com.kinghy.rag.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

/**
 * 聊天消息数据传输对象
 * 
 * @author yunzhongxiaoma
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDTO implements Serializable {
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 会话ID（可选，如果不提供则使用当前会话）
     */
    private String sessionId;
    
    /**
     * 消息元数据
     */
    private Map<String, Object> metadata;
}