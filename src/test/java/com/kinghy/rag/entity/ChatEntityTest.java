package com.kinghy.rag.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 聊天实体类测试
 */
public class ChatEntityTest {

    @Test
    public void testChatSessionEntity() {
        // Test ChatSession entity creation and basic operations
        LocalDateTime now = LocalDateTime.now();
        
        ChatSession session = ChatSession.builder()
                .sessionId("test-session-123")
                .userId(666497)
                .title("Test Chat Session")
                .status(1)
                .messageCount(0)
                .lastMessageTime(now)
                .createTime(now)
                .updateTime(now)
                .build();

        assertNotNull(session);
        assertEquals("test-session-123", session.getSessionId());
        assertEquals(Integer.valueOf(666497), session.getUserId());
        assertEquals("Test Chat Session", session.getTitle());
        assertEquals(Integer.valueOf(1), session.getStatus());
        assertEquals(Integer.valueOf(0), session.getMessageCount());
        assertEquals(now, session.getLastMessageTime());
        assertEquals(now, session.getCreateTime());
        assertEquals(now, session.getUpdateTime());
    }

    @Test
    public void testChatMessageEntity() {
        // Test ChatMessage entity creation and basic operations
        LocalDateTime now = LocalDateTime.now();
        
        ChatMessage message = ChatMessage.builder()
                .sessionId("test-session-123")
                .userId(666497)
                .messageType(ChatMessage.MessageType.USER)
                .content("Hello, this is a test message")
                .metadata("{\"source\": \"test\"}")
                .createTime(now)
                .build();

        assertNotNull(message);
        assertEquals("test-session-123", message.getSessionId());
        assertEquals(Integer.valueOf(666497), message.getUserId());
        assertEquals(ChatMessage.MessageType.USER, message.getMessageType());
        assertEquals("Hello, this is a test message", message.getContent());
        assertEquals("{\"source\": \"test\"}", message.getMetadata());
        assertEquals(now, message.getCreateTime());
    }

    @Test
    public void testMessageTypeEnum() {
        // Test MessageType enum values
        assertEquals("USER", ChatMessage.MessageType.USER.name());
        assertEquals("ASSISTANT", ChatMessage.MessageType.ASSISTANT.name());
        
        // Test enum values array
        ChatMessage.MessageType[] types = ChatMessage.MessageType.values();
        assertEquals(2, types.length);
        assertTrue(java.util.Arrays.asList(types).contains(ChatMessage.MessageType.USER));
        assertTrue(java.util.Arrays.asList(types).contains(ChatMessage.MessageType.ASSISTANT));
    }

    @Test
    public void testEntityEqualsAndHashCode() {
        // Test equals and hashCode methods (provided by Lombok @Data)
        LocalDateTime now = LocalDateTime.now();
        
        ChatSession session1 = ChatSession.builder()
                .sessionId("test-session-123")
                .userId(666497)
                .title("Test Session")
                .status(1)
                .messageCount(0)
                .createTime(now)
                .build();

        ChatSession session2 = ChatSession.builder()
                .sessionId("test-session-123")
                .userId(666497)
                .title("Test Session")
                .status(1)
                .messageCount(0)
                .createTime(now)
                .build();

        assertEquals(session1, session2);
        assertEquals(session1.hashCode(), session2.hashCode());
    }
}