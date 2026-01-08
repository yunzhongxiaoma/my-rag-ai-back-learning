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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
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
import java.util.List;

@Tag(name = "AiRagController", description = "Rag接口")
@Slf4j
@RestController
@RequestMapping(ApplicationConstant.API_VERSION + "/ai")
public class AiRagController {

    // 对话代理
    ChatClient chatClient;
    VectorStore vectorStore;
    @Autowired
    private SensitiveWordService sensitiveWordService;

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
        this.vectorStore = vectorStore;
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
                .advisors(QuestionAnswerAdvisor.builder(vectorStore)
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
}