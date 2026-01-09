/*
 * Copyright 2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.kinghy.rag.controller;

import com.kinghy.rag.annotation.Loggable;
import com.kinghy.rag.common.ApplicationConstant;
import com.kinghy.rag.context.BaseContext;
import com.kinghy.rag.entity.SensitiveWord;
import com.kinghy.rag.service.SensitiveWordService;
import com.kinghy.rag.service.VectorStoreManager;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "AiRagController", description = "Rag接口")
@Slf4j
@RestController
@RequestMapping(ApplicationConstant.API_VERSION + "/ai")
public class AiRagController {

    // 对话代理
    ChatClient chatClient;
    VectorStore defaultVectorStore;
    
    @Autowired
    private SensitiveWordService sensitiveWordService;
    
    @Autowired
    private VectorStoreManager vectorStoreManager;

    public AiRagController(ChatModel chatModel, ChatMemory chatMemory,
                           VectorStore vectorStore) {
        this.chatClient = ChatClient.builder(chatModel)
                // 隐式
                .defaultSystem("""
                        你是“XX”知识库系统的对话助手，请以乐于助人的方式进行对话
                        今天的日期：{current_data}
                        """)
                .defaultAdvisors(
                        PromptChatMemoryAdvisor.builder(chatMemory).build(),
                        SimpleLoggerAdvisor.builder().build()
                )
                .build();
        this.defaultVectorStore = vectorStore;
    }

    @Operation(summary = "rag", description = "Rag对话接口")
    @GetMapping(value = "/rag", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Loggable
    public Flux<String> generate(@RequestParam(value = "message", defaultValue = "你好") String message) throws IOException {

        // 敏感词过滤
        List<SensitiveWord> list = sensitiveWordService.list();

        for (SensitiveWord sensitiveWord : list) {
            if (message.contains(sensitiveWord.getWord())) {
                return Flux.just("包含敏感词:" + sensitiveWord.getWord());
            }
        }

        Long userId = BaseContext.getCurrentId();
        Flux<String> content = chatClient.prompt()
                .user(message)  // 用户提示词 显式
                .advisors(a -> a.param("current_data", LocalDate.now().toString()))
                //.call() // 同步方式
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                .advisors(QuestionAnswerAdvisor.builder(defaultVectorStore)
                        .searchRequest(
                                SearchRequest.builder()
                                        .query(message)
                                        .similarityThreshold(0.1d).topK(5)
                                        .build()
                        )
                        .build())
                .stream()// 流式方式
                .content();

        return content;
    }
    
    @Operation(summary = "ragWithKnowledgeBases", description = "基于指定知识库的RAG对话接口")
    @GetMapping(value = "/rag/knowledge-bases", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Loggable
    public Flux<String> generateWithKnowledgeBases(
            @RequestParam(value = "message", defaultValue = "你好") String message,
            @RequestParam(value = "knowledgeBaseIds") List<Long> knowledgeBaseIds) throws IOException {

        log.info("基于知识库进行RAG对话，知识库ID列表: {}, 消息: {}", knowledgeBaseIds, message);
        
        // 敏感词过滤
        List<SensitiveWord> list = sensitiveWordService.list();
        for (SensitiveWord sensitiveWord : list) {
            if (message.contains(sensitiveWord.getWord())) {
                return Flux.just("包含敏感词:" + sensitiveWord.getWord());
            }
        }

        // 验证知识库ID列表
        if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty()) {
            log.warn("知识库ID列表为空，使用默认向量存储");
            return generate(message);
        }

        Long userId = BaseContext.getCurrentId();
        
        try {
            // 在指定知识库中进行相似性搜索
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(message)
                    .similarityThreshold(0.1d)
                    .topK(5)
                    .build();
            
            List<Document> searchResults = vectorStoreManager.similaritySearch(knowledgeBaseIds, searchRequest);
            
            // 构建上下文信息
            StringBuilder contextBuilder = new StringBuilder();
            for (Document doc : searchResults) {
                // 使用Document的getContent()方法，如果不存在则使用toString()
                String content = doc.toString(); // 临时使用toString()，后续可能需要调整
                contextBuilder.append(content).append("\n\n");
            }
            String context = contextBuilder.toString();
            
            // 构建增强的提示词
            String enhancedMessage = message;
            if (!context.isEmpty()) {
                enhancedMessage = String.format("""
                        基于以下知识库内容回答问题：
                        
                        知识库内容：
                        %s
                        
                        用户问题：%s
                        
                        请基于上述知识库内容回答用户问题。如果知识库内容无法回答问题，请说明并提供一般性建议。
                        """, context, message);
            }
            
            // 生成回答
            Flux<String> content = chatClient.prompt()
                    .user(enhancedMessage)
                    .advisors(a -> a.param("current_data", LocalDate.now().toString()))
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                    .stream()
                    .content();

            return content;
            
        } catch (Exception e) {
            log.error("基于知识库的RAG对话失败", e);
            return Flux.just("抱歉，处理您的问题时出现了错误，请稍后重试。");
        }
    }
}