package com.kinghy.rag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import static org.mockito.Mockito.mock;

/**
 * 聊天功能测试配置类 - 提供AI组件的Mock实现
 * 
 * @author yunzhongxiaoma
 */
@TestConfiguration
public class ChatTestConfiguration {

    @Bean
    @Primary
    public VectorStore mockVectorStore() {
        return mock(VectorStore.class);
    }

    @Bean
    @Primary
    public ChatModel mockChatModel() {
        return mock(ChatModel.class);
    }

    @Bean
    @Primary
    public ChatClient mockChatClient() {
        return mock(ChatClient.class);
    }
}