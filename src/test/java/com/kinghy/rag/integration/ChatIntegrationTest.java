package com.kinghy.rag.integration;

import com.kinghy.rag.config.ChatTestConfiguration;
import com.kinghy.rag.entity.ChatMessage;
import com.kinghy.rag.entity.ChatSession;
import com.kinghy.rag.service.ChatMessageService;
import com.kinghy.rag.service.ChatSessionService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 聊天功能集成测试
 * 
 * @author yunzhongxiaoma
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Import(ChatTestConfiguration.class)
@Slf4j
@Transactional
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=always"
})
public class ChatIntegrationTest {

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatMessageService chatMessageService;

    @Test
    public void testCompleteChatFlow() {
        // 测试用户ID
        Integer userId = 1;
        
        log.info("Starting complete chat flow test for user: {}", userId);
        
        // 1. 创建新会话
        ChatSession session = chatSessionService.createNewSession(userId);
        assertNotNull(session);
        assertNotNull(session.getSessionId());
        assertEquals(userId, session.getUserId());
        assertEquals("新对话", session.getTitle());
        log.info("Created session: {}", session.getSessionId());
        
        // 2. 验证当前会话
        ChatSession currentSession = chatSessionService.getCurrentSession(userId);
        assertNotNull(currentSession);
        assertEquals(session.getSessionId(), currentSession.getSessionId());
        log.info("Verified current session: {}", currentSession.getSessionId());
        
        // 3. 保存用户消息
        String userMessageContent = "Hello, this is a test message";
        ChatMessage userMessage = chatMessageService.saveUserMessage(
                session.getSessionId(), userId, userMessageContent);
        assertNotNull(userMessage);
        assertEquals(ChatMessage.MessageType.USER, userMessage.getMessageType());
        assertEquals(userMessageContent, userMessage.getContent());
        log.info("Saved user message: {}", userMessage.getId());
        
        // 4. 保存AI响应
        String aiResponseContent = "Hello! I'm an AI assistant. How can I help you today?";
        ChatMessage aiMessage = chatMessageService.saveAssistantMessage(
                session.getSessionId(), userId, aiResponseContent, null);
        assertNotNull(aiMessage);
        assertEquals(ChatMessage.MessageType.ASSISTANT, aiMessage.getMessageType());
        assertEquals(aiResponseContent, aiMessage.getContent());
        log.info("Saved AI message: {}", aiMessage.getId());
        
        // 5. 获取会话消息
        List<ChatMessage> messages = chatMessageService.getSessionMessages(session.getSessionId(), userId);
        assertEquals(2, messages.size());
        
        // 验证消息顺序（按时间升序）
        assertEquals(ChatMessage.MessageType.USER, messages.get(0).getMessageType());
        assertEquals(ChatMessage.MessageType.ASSISTANT, messages.get(1).getMessageType());
        log.info("Retrieved {} messages from session", messages.size());
        
        // 6. 获取最近消息
        List<ChatMessage> recentMessages = chatMessageService.getRecentMessages(
                session.getSessionId(), userId, 10);
        assertEquals(2, recentMessages.size());
        log.info("Retrieved {} recent messages", recentMessages.size());
        
        // 7. 获取用户会话列表
        List<ChatSession> userSessions = chatSessionService.getUserSessions(userId, 1, 10);
        assertTrue(userSessions.size() >= 1);
        
        // 验证会话在列表中
        boolean sessionFound = userSessions.stream()
                .anyMatch(s -> s.getSessionId().equals(session.getSessionId()));
        assertTrue(sessionFound);
        log.info("Found session in user sessions list");
        
        // 8. 更新会话标题
        String newTitle = "Test Conversation";
        chatSessionService.updateSessionTitle(session.getSessionId(), userId, newTitle);
        
        ChatSession updatedSession = chatSessionService.getSessionById(session.getSessionId(), userId);
        assertEquals(newTitle, updatedSession.getTitle());
        log.info("Updated session title to: {}", newTitle);
        
        // 9. 测试消息计数
        long messageCount = chatMessageService.getMessageCount(session.getSessionId(), userId);
        assertEquals(2, messageCount);
        log.info("Message count: {}", messageCount);
        
        // 10. 创建第二个会话测试会话切换
        ChatSession session2 = chatSessionService.createNewSession(userId);
        assertNotNull(session2);
        assertNotEquals(session.getSessionId(), session2.getSessionId());
        
        // 验证新会话成为当前会话
        ChatSession newCurrentSession = chatSessionService.getCurrentSession(userId);
        assertEquals(session2.getSessionId(), newCurrentSession.getSessionId());
        log.info("Created second session and verified session switching");
        
        // 11. 激活第一个会话
        chatSessionService.activateSession(session.getSessionId(), userId);
        ChatSession reactivatedSession = chatSessionService.getCurrentSession(userId);
        assertEquals(session.getSessionId(), reactivatedSession.getSessionId());
        log.info("Reactivated first session");
        
        log.info("Complete chat flow test completed successfully");
    }

    @Test
    public void testUserDataIsolation() {
        // 测试用户数据隔离
        Integer user1 = 1;
        Integer user2 = 2;
        
        log.info("Testing user data isolation between users {} and {}", user1, user2);
        
        // 用户1创建会话和消息
        ChatSession session1 = chatSessionService.createNewSession(user1);
        chatMessageService.saveUserMessage(session1.getSessionId(), user1, "User 1 message");
        
        // 用户2创建会话和消息
        ChatSession session2 = chatSessionService.createNewSession(user2);
        chatMessageService.saveUserMessage(session2.getSessionId(), user2, "User 2 message");
        
        // 验证用户1无法访问用户2的会话
        ChatSession user2SessionFromUser1 = chatSessionService.getSessionById(session2.getSessionId(), user1);
        assertNull(user2SessionFromUser1);
        
        // 验证用户2无法访问用户1的会话
        ChatSession user1SessionFromUser2 = chatSessionService.getSessionById(session1.getSessionId(), user2);
        assertNull(user1SessionFromUser2);
        
        // 验证用户1无法获取用户2的消息
        List<ChatMessage> user2MessagesFromUser1 = chatMessageService.getSessionMessages(
                session2.getSessionId(), user1);
        assertTrue(user2MessagesFromUser1.isEmpty());
        
        // 验证用户2无法获取用户1的消息
        List<ChatMessage> user1MessagesFromUser2 = chatMessageService.getSessionMessages(
                session1.getSessionId(), user2);
        assertTrue(user1MessagesFromUser2.isEmpty());
        
        log.info("User data isolation test completed successfully");
    }

    @Test
    public void testPaginationFunctionality() {
        Integer userId = 1;
        
        log.info("Testing pagination functionality for user: {}", userId);
        
        // 创建会话
        ChatSession session = chatSessionService.createNewSession(userId);
        
        // 创建多条消息
        for (int i = 1; i <= 25; i++) {
            chatMessageService.saveUserMessage(session.getSessionId(), userId, "Message " + i);
            chatMessageService.saveAssistantMessage(session.getSessionId(), userId, "Response " + i, null);
        }
        
        // 测试分页获取消息
        List<ChatMessage> page1 = chatMessageService.getSessionMessages(session.getSessionId(), userId, 1, 10);
        assertEquals(10, page1.size());
        
        List<ChatMessage> page2 = chatMessageService.getSessionMessages(session.getSessionId(), userId, 2, 10);
        assertEquals(10, page2.size());
        
        List<ChatMessage> page3 = chatMessageService.getSessionMessages(session.getSessionId(), userId, 3, 10);
        assertEquals(10, page3.size());
        
        // 验证消息不重复
        assertNotEquals(page1.get(0).getId(), page2.get(0).getId());
        assertNotEquals(page2.get(0).getId(), page3.get(0).getId());
        
        // 测试最近消息限制
        List<ChatMessage> recentMessages = chatMessageService.getRecentMessages(
                session.getSessionId(), userId, 15);
        assertEquals(15, recentMessages.size());
        
        log.info("Pagination functionality test completed successfully");
    }
}