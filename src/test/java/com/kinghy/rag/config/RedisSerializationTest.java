package com.kinghy.rag.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kinghy.rag.entity.ChatMessage;
import com.kinghy.rag.entity.ChatSession;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Redis 序列化配置测试类
 * 验证 Redis 序列化器配置是否正确
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
public class RedisSerializationTest {

    @Test
    public void testChatMessageSerialization() throws Exception {
        // 创建测试消息
        ChatMessage message = ChatMessage.builder()
                .id(1L)
                .sessionId("test-session-123")
                .userId(1)
                .messageType(ChatMessage.MessageType.USER)
                .content("这是一条测试消息")
                .metadata("{\"test\": \"metadata\"}")
                .createTime(LocalDateTime.now())
                .build();

        // 创建支持 Java 8 时间类型和类型信息的序列化器
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator.instance, 
                ObjectMapper.DefaultTyping.NON_FINAL);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // 序列化
        byte[] serialized = serializer.serialize(message);
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
        
        // 反序列化
        Object deserialized = serializer.deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ChatMessage);
        
        ChatMessage deserializedMessage = (ChatMessage) deserialized;
        assertEquals(message.getId(), deserializedMessage.getId());
        assertEquals(message.getSessionId(), deserializedMessage.getSessionId());
        assertEquals(message.getContent(), deserializedMessage.getContent());
        assertEquals(message.getMessageType(), deserializedMessage.getMessageType());
    }

    @Test
    public void testChatSessionSerialization() throws Exception {
        // 创建测试会话
        ChatSession session = ChatSession.builder()
                .id(1L)
                .sessionId("test-session-456")
                .userId(1)
                .title("测试会话")
                .status(1)
                .messageCount(5)
                .lastMessageTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        // 创建支持 Java 8 时间类型和类型信息的序列化器
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator.instance, 
                ObjectMapper.DefaultTyping.NON_FINAL);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // 序列化
        byte[] serialized = serializer.serialize(session);
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
        
        // 反序列化
        Object deserialized = serializer.deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(deserialized instanceof ChatSession);
        
        ChatSession deserializedSession = (ChatSession) deserialized;
        assertEquals(session.getId(), deserializedSession.getId());
        assertEquals(session.getSessionId(), deserializedSession.getSessionId());
        assertEquals(session.getTitle(), deserializedSession.getTitle());
        assertEquals(session.getStatus(), deserializedSession.getStatus());
    }

    @Test
    public void testMessageListSerialization() throws Exception {
        // 创建测试消息列表
        List<ChatMessage> messages = new ArrayList<>();
        
        for (int i = 1; i <= 3; i++) {
            ChatMessage message = ChatMessage.builder()
                    .id((long) i)
                    .sessionId("test-session-789")
                    .userId(1)
                    .messageType(i % 2 == 1 ? ChatMessage.MessageType.USER : ChatMessage.MessageType.ASSISTANT)
                    .content("测试消息 " + i)
                    .createTime(LocalDateTime.now())
                    .build();
            messages.add(message);
        }

        // 创建支持 Java 8 时间类型和类型信息的序列化器
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator.instance, 
                ObjectMapper.DefaultTyping.NON_FINAL);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        // 序列化
        byte[] serialized = serializer.serialize(messages);
        assertNotNull(serialized);
        assertTrue(serialized.length > 0);
        
        // 反序列化
        Object deserialized = serializer.deserialize(serialized);
        assertNotNull(deserialized);
        assertTrue(deserialized instanceof List);
        
        @SuppressWarnings("unchecked")
        List<ChatMessage> deserializedMessages = (List<ChatMessage>) deserialized;
        assertEquals(messages.size(), deserializedMessages.size());
        
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage original = messages.get(i);
            ChatMessage deserialized_msg = deserializedMessages.get(i);
            assertEquals(original.getId(), deserialized_msg.getId());
            assertEquals(original.getContent(), deserialized_msg.getContent());
            assertEquals(original.getMessageType(), deserialized_msg.getMessageType());
        }
    }

    @Test
    public void testStringSerializationStillWorks() throws Exception {
        // 验证字符串序列化仍然正常工作（向后兼容性）
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator.instance, 
                ObjectMapper.DefaultTyping.NON_FINAL);
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        
        String testString = "这是一个测试字符串";
        
        // 序列化
        byte[] serialized = serializer.serialize(testString);
        assertNotNull(serialized);
        
        // 反序列化
        Object deserialized = serializer.deserialize(serialized);
        assertNotNull(deserialized);
        assertEquals(testString, deserialized);
    }

    @Test
    public void testSerializerConfiguration() {
        // 验证序列化器配置正确
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        assertNotNull(stringSerializer);
        
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        assertNotNull(jsonSerializer);
    }
}