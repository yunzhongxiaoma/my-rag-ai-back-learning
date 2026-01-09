package com.kinghy.rag.service;

import com.kinghy.rag.entity.ChatSession;
import com.kinghy.rag.mapper.ChatSessionMapper;
import com.kinghy.rag.service.impl.ChatSessionServiceImpl;
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
 * ChatSessionService 测试类
 */
@ExtendWith(MockitoExtension.class)
public class ChatSessionServiceTest {

    @Mock
    private ChatSessionMapper chatSessionMapper;

    @InjectMocks
    private ChatSessionServiceImpl chatSessionService;

    private ChatSession testSession;
    private Integer testUserId = 666497;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        testSession = ChatSession.builder()
                .id(1L)
                .sessionId("test-session-123")
                .userId(testUserId)
                .title("Test Session")
                .status(1)
                .messageCount(0)
                .lastMessageTime(now)
                .createTime(now)
                .updateTime(now)
                .build();
    }

    @Test
    void testCreateNewSession() {
        // Given
        when(chatSessionMapper.insert(any(ChatSession.class))).thenReturn(1);

        // When
        ChatSession result = chatSessionService.createNewSession(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals("新对话", result.getTitle());
        assertEquals(Integer.valueOf(1), result.getStatus());
        assertEquals(Integer.valueOf(0), result.getMessageCount());
        assertNotNull(result.getSessionId());
        assertNotNull(result.getCreateTime());
        assertNotNull(result.getUpdateTime());

        // Verify interactions
        verify(chatSessionMapper).deactivateOtherSessions(eq(testUserId), anyString());
        verify(chatSessionMapper).insert(any(ChatSession.class));
    }

    @Test
    void testGetCurrentSession() {
        // Given
        when(chatSessionMapper.getCurrentSessionByUserId(testUserId)).thenReturn(testSession);

        // When
        ChatSession result = chatSessionService.getCurrentSession(testUserId);

        // Then
        assertNotNull(result);
        assertEquals(testSession.getSessionId(), result.getSessionId());
        assertEquals(testUserId, result.getUserId());

        // Verify interactions
        verify(chatSessionMapper).getCurrentSessionByUserId(testUserId);
    }

    @Test
    void testGetSessionById() {
        // Given
        String sessionId = "test-session-123";
        when(chatSessionMapper.getSessionByIdAndUserId(sessionId, testUserId)).thenReturn(testSession);

        // When
        ChatSession result = chatSessionService.getSessionById(sessionId, testUserId);

        // Then
        assertNotNull(result);
        assertEquals(sessionId, result.getSessionId());
        assertEquals(testUserId, result.getUserId());

        // Verify interactions
        verify(chatSessionMapper).getSessionByIdAndUserId(sessionId, testUserId);
    }

    @Test
    void testGetUserSessions() {
        // Given
        List<ChatSession> sessions = Arrays.asList(testSession);
        when(chatSessionMapper.getUserSessionsPaged(testUserId, 0, 10)).thenReturn(sessions);

        // When
        List<ChatSession> result = chatSessionService.getUserSessions(testUserId, 1, 10);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testSession.getSessionId(), result.get(0).getSessionId());

        // Verify interactions
        verify(chatSessionMapper).getUserSessionsPaged(testUserId, 0, 10);
    }

    @Test
    void testUpdateSessionTitle() {
        // Given
        String sessionId = "test-session-123";
        Integer userId = 1;
        String newTitle = "Updated Title";

        // When
        chatSessionService.updateSessionTitle(sessionId, userId, newTitle);

        // Then
        verify(chatSessionMapper).updateSessionTitle(sessionId, newTitle);
    }

    @Test
    void testEndSession() {
        // Given
        String sessionId = "test-session-123";

        // When
        chatSessionService.endSession(sessionId, testUserId);

        // Then
        verify(chatSessionMapper).endSession(sessionId, testUserId);
    }

    @Test
    void testDeleteSession() {
        // Given
        String sessionId = "test-session-123";
        when(chatSessionMapper.getSessionByIdAndUserId(sessionId, testUserId)).thenReturn(testSession);

        // When
        chatSessionService.deleteSession(sessionId, testUserId);

        // Then
        verify(chatSessionMapper).getSessionByIdAndUserId(sessionId, testUserId);
        verify(chatSessionMapper).deleteById((Long) testSession.getId());
    }

    @Test
    void testDeleteSessionNotFound() {
        // Given
        String sessionId = "non-existent-session";
        when(chatSessionMapper.getSessionByIdAndUserId(sessionId, testUserId)).thenReturn(null);

        // When
        chatSessionService.deleteSession(sessionId, testUserId);

        // Then
        verify(chatSessionMapper).getSessionByIdAndUserId(sessionId, testUserId);
        verify(chatSessionMapper, never()).deleteById(anyLong());
    }

    @Test
    void testIncrementMessageCount() {
        // Given
        String sessionId = "test-session-123";

        // When
        chatSessionService.incrementMessageCount(sessionId);

        // Then
        verify(chatSessionMapper).incrementMessageCount(sessionId);
    }

    @Test
    void testUpdateLastMessageTime() {
        // Given
        String sessionId = "test-session-123";

        // When
        chatSessionService.updateLastMessageTime(sessionId);

        // Then
        verify(chatSessionMapper).updateLastMessageTime(sessionId);
    }

    @Test
    void testActivateSession() {
        // Given
        String sessionId = "test-session-123";
        when(chatSessionMapper.getSessionByIdAndUserId(sessionId, testUserId)).thenReturn(testSession);

        // When
        chatSessionService.activateSession(sessionId, testUserId);

        // Then
        verify(chatSessionMapper).getSessionByIdAndUserId(sessionId, testUserId);
        verify(chatSessionMapper).deactivateOtherSessions(testUserId, sessionId);
        verify(chatSessionMapper).activateSession(sessionId, testUserId);
    }

    @Test
    void testActivateSessionNotFound() {
        // Given
        String sessionId = "non-existent-session";
        when(chatSessionMapper.getSessionByIdAndUserId(sessionId, testUserId)).thenReturn(null);

        // When
        chatSessionService.activateSession(sessionId, testUserId);

        // Then
        verify(chatSessionMapper).getSessionByIdAndUserId(sessionId, testUserId);
        verify(chatSessionMapper, never()).deactivateOtherSessions(any(), any());
        verify(chatSessionMapper, never()).activateSession(any(), any());
    }
}