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
 * 聊天会话实体类
 * 
 * @TableName chat_session
 */
@TableName(value = "chat_session")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession implements Serializable {
    
    private static final long serialVersionUID = 1L;
    /**
     * 会话ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 会话唯一标识符
     */
    private String sessionId;

    /**
     * 用户ID，关联tb_user表
     */
    private Integer userId;

    /**
     * 会话标题（基于首条消息生成）
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
     * 更新时间
     */
    private LocalDateTime updateTime;
}