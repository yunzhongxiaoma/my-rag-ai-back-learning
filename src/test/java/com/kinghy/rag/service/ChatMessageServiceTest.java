package com.kinghy.rag.service;

import com.kinghy.rag.entity.ChatMessage;
import com.kinghy.rag.mapper.ChatMessageMapper;
import com.kinghy.rag.service.impl.ChatMessageServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ChatMessageService 测试类
 */
@ExtendWith(MockitoExtension.class)
public class ChatMessageServiceTest {

    @Mock
    private ChatMessageMapper chatMessageMapper;

    @Mock
    private ChatSessionService chatSessionService;

    @InjectMocks
    private ChatMessageServiceImpl chatMessageService;

    private ChatMessage testUserMessage;
    private ChatMessage testAssistantMessage;
    private String testSessionId = "test-session-123";
    private Integer testUserId = 666497;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        
        testUserMessage = ChatMessage.builder()
                .id(1L)
                .sessionId(testSessionId)
                .userId(testUserId)
                .messageType(ChatMessage.MessageType.USER)
                .content("Hello, this is a test user message")
                .metadata(null)
                .createTime(now)
                .build();

        testAssistantMessage = ChatMessage.builder()
                .id(2L)
                .sessionId(testSessionId)
                .userId(testUserId)
                .messageType(ChatMessage.MessageType.ASSISTANT)
                .content("Hello, this is a test assistant response")
                .metadata("{\"model\": \"test-model\", \"tokens\": 10}")
                .createTime(now.plusMinutes(1))
                .build();
    }

    @Test
    void testSaveUserMessage() {
        // Given
        String content = "Test user message";
        doReturn(1).when(chatMessageMapper).insert(any(ChatMessage.class));

        // When
        ChatMessage result = chatMessageService.saveUserMessage(testSessionId, testUserId, content);

        // Then
        assertNotNull(result);
        assertEquals(testSessionId, result.getSessionId());
        assertEquals(testUserId, result.getUserId());
        assertEquals(ChatMessage.MessageType.USER, result.getMessageType());
        assertEquals(content, result.getContent());
        assertNull(result.getMetadata());
        assertNotNull(result.getCreateTime());

        // Verify interactions
        verify(chatMessageMapper).insert(any(ChatMessage.class));
        verify(chatSessionService).incrementMessageCount(testSessionId);
        verify(chatSessionService).updateLastMessageTime(testSessionId);
    }

    @Test
    void testSaveAssistantMessage() {
        // Given
        String content = "Test assistant response";
        String metadata = "{\"model\": \"gpt-4\", \"tokens\": 20}";
        doReturn(1).when(chatMessageMapper).insert(any(ChatMessage.class));

        // When
        ChatMessage result = chatMessageService.saveAssistantMessage(testSessionId, testUserId, content, metadata);

        // Then
        assertNotNull(result);
        assertEquals(testSessionId, result.getSessionId());
        assertEquals(testUserId, result.getUserId());
        assertEquals(ChatMessage.MessageType.ASSISTANT, result.getMessageType());
        assertEquals(content, result.getContent());
        assertEquals(metadata, result.getMetadata());
        assertNotNull(result.getCreateTime());

        // Verify interactions
        verify(chatMessageMapper).insert(any(ChatMessage.class));
        verify(chatSessionService).incrementMessageCount(testSessionId);
        verify(chatSessionService).updateLastMessageTime(testSessionId);
    }

    @Test
    void testGetSessionMessages() {
        // Given
        List<ChatMessage> messages = Arrays.asList(testUserMessage, testAssistantMessage);
        when(chatMessageMapper.getSessionMessages(testSessionId, testUserId)).thenReturn(messages);

        // When
        List<ChatMessage> result = chatMessageService.getSessionMessages(testSessionId, testUserId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testUserMessage.getId(), result.get(0).getId());
        assertEquals(testAssistantMessage.getId(), result.get(1).getId());

        // Verify interactions
        verify(chatMessageMapper).getSessionMessages(testSessionId, testUserId);
    }

    @Test
    void testGetRecentMessages() {
        // Given
        List<ChatMessage> messages = Arrays.asList(testAssistantMessage, testUserMessage);
        when(chatMessageMapper.getRecentMessages(testSessionId, testUserId, 5)).thenReturn(messages);

        // When
        List<ChatMessage> result = chatMessageService.getRecentMessages(testSessionId, testUserId, 5);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(testAssistantMessage.getId(), result.get(0).getId());

        // Verify interactions
        verify(chatMessageMapper).getRecentMessages(testSessionId, testUserId, 5);
    }

    @Test
    void testGetMessageCount() {
        // Given
        long expectedCount = 5L;
        when(chatMessageMapper.getMessageCount(testSessionId, testUserId)).thenReturn(expectedCount);

        // When
        long result = chatMessageService.getMessageCount(testSessionId, testUserId);

        // Then
        assertEquals(expectedCount, result);
        verify(chatMessageMapper).getMessageCount(testSessionId, testUserId);
    }
}