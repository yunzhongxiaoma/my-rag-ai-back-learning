package com.kinghy.rag.controller;

import com.kinghy.rag.annotation.Loggable;
import com.kinghy.rag.common.ApplicationConstant;
import com.kinghy.rag.common.BaseResponse;
import com.kinghy.rag.common.ResultUtils;
import com.kinghy.rag.context.BaseContext;
import com.kinghy.rag.entity.ChatMessage;
import com.kinghy.rag.entity.ChatSession;
import com.kinghy.rag.entity.SensitiveWord;
import com.kinghy.rag.pojo.dto.ChatMessageDTO;
import com.kinghy.rag.pojo.vo.ChatMessageVO;
import com.kinghy.rag.service.ChatMessageService;
import com.kinghy.rag.service.ChatSessionService;
import com.kinghy.rag.service.SensitiveWordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Title: ChatController
 * @Author kinghy
 * @Package com.kinghy.rag.controller
 * @description: 对话接口
 */

@Tag(name="ChatController",description = "chat对话接口")
@Slf4j
@RestController
@RequestMapping(ApplicationConstant.API_VERSION + "/chat")
public class ChatController {

    @Autowired
    private ChatClient chatClient;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatMessageService chatMessageService;

    public ChatController(ChatClient.Builder builder, ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultSystem("""
                        你是一家名为"XX公司"的知识库系统的客户客服代理。请友好乐于助人，充满喜悦地回复。
                        """)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build() // CHAT MEMORY
                )
                .build();
    }

    @Operation(summary = "stream", description = "流式对话接口")
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Loggable("message")
    public Flux<String> streamRagChat(@RequestParam(value = "message", defaultValue = "你好") String message,
                                      @RequestParam(value = "prompt", defaultValue = "你是一名AI助手，致力于帮助人们解决问题.") String prompt,
                                      @RequestParam(value = "sessionId", required = false) String sessionId) {
        
        // 敏感词检查
        List<SensitiveWord> list = sensitiveWordService.list();
        for (SensitiveWord sensitiveWord : list) {
            if (message.contains(sensitiveWord.getWord())) {
                return Flux.just("包含敏感词:" + sensitiveWord.getWord());
            }
        }

        Integer userId = BaseContext.getCurrentId().intValue();
        
        // 获取或创建会话
        ChatSession currentSession = getCurrentOrCreateSession(userId, sessionId);
        String activeSessionId = currentSession.getSessionId();
        
        // 保存用户消息
        chatMessageService.saveUserMessage(activeSessionId, userId, message);
        
        // 生成AI响应并保存
        StringBuilder responseBuilder = new StringBuilder();
        
        return chatClient.prompt()
                .system(prompt)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                .user(message)
                .stream()
                .content()
                .doOnNext(chunk -> {
                    // 累积响应内容
                    responseBuilder.append(chunk);
                })
                .doOnComplete(() -> {
                    // 流式响应完成后，保存完整的AI响应
                    String fullResponse = responseBuilder.toString();
                    if (StringUtils.hasText(fullResponse)) {
                        chatMessageService.saveAssistantMessage(activeSessionId, userId, fullResponse, null);
                        log.info("保存AI响应消息，会话ID: {}, 用户ID: {}, 响应长度: {}", 
                                activeSessionId, userId, fullResponse.length());
                    }
                })
                .doOnError(error -> {
                    log.error("流式对话过程中发生错误，会话ID: {}, 用户ID: {}", activeSessionId, userId, error);
                });
    }

    /**
     * 发送消息并获取响应（非流式）
     */
    @PostMapping("/message")
    @Operation(summary = "发送消息", description = "发送消息并获取AI响应")
    public BaseResponse<ChatMessageVO> sendMessage(@RequestBody ChatMessageDTO messageDTO) {
        log.info("发送消息，内容: {}, 会话ID: {}", messageDTO.getContent(), messageDTO.getSessionId());
        
        // 敏感词检查
        List<SensitiveWord> list = sensitiveWordService.list();
        for (SensitiveWord sensitiveWord : list) {
            if (messageDTO.getContent().contains(sensitiveWord.getWord())) {
                return ResultUtils.error("消息包含敏感词: " + sensitiveWord.getWord());
            }
        }

        Integer userId = BaseContext.getCurrentId().intValue();
        
        // 获取或创建会话
        ChatSession currentSession = getCurrentOrCreateSession(userId, messageDTO.getSessionId());
        String activeSessionId = currentSession.getSessionId();
        
        // 保存用户消息
        ChatMessage userMessage = chatMessageService.saveUserMessage(activeSessionId, userId, messageDTO.getContent());
        
        try {
            // 生成AI响应
            String aiResponse = chatClient.prompt()
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, userId))
                    .user(messageDTO.getContent())
                    .call()
                    .content();
            
            // 保存AI响应
            ChatMessage assistantMessage = chatMessageService.saveAssistantMessage(
                    activeSessionId, userId, aiResponse, null);
            
            // 返回AI响应
            ChatMessageVO responseVO = convertToVO(assistantMessage);
            return ResultUtils.success(responseVO);
            
        } catch (Exception e) {
            log.error("生成AI响应时发生错误，会话ID: {}, 用户ID: {}", activeSessionId, userId, e);
            return ResultUtils.error("生成响应时发生错误，请稍后重试");
        }
    }

    /**
     * 获取聊天历史记录
     */
    @GetMapping("/history")
    @Operation(summary = "获取聊天历史", description = "获取指定会话的聊天历史记录")
    public BaseResponse<List<ChatMessageVO>> getChatHistory(
            @RequestParam(required = false) String sessionId,
            @RequestParam(defaultValue = "50") int limit) {
        
        log.info("获取聊天历史，会话ID: {}, 限制数量: {}, 用户ID: {}", 
                sessionId, limit, BaseContext.getCurrentId());
        
        Integer userId = BaseContext.getCurrentId().intValue();
        
        // 如果没有指定会话ID，获取当前活跃会话
        if (!StringUtils.hasText(sessionId)) {
            ChatSession currentSession = chatSessionService.getCurrentSession(userId);
            if (currentSession == null) {
                // 没有活跃会话，返回空列表
                return ResultUtils.success(List.of());
            }
            sessionId = currentSession.getSessionId();
        }
        
        // 验证会话权限
        ChatSession session = chatSessionService.getSessionById(sessionId, userId);
        if (session == null) {
            return ResultUtils.error("会话不存在或无权限访问");
        }
        
        // 获取消息历史
        List<ChatMessage> messages = chatMessageService.getRecentMessages(sessionId, userId, limit);
        
        // 转换为VO并按时间正序排列（最早的消息在前）
        List<ChatMessageVO> messageVOs = messages.stream()
                .map(this::convertToVO)
                .sorted((m1, m2) -> m1.getCreateTime().compareTo(m2.getCreateTime()))
                .collect(Collectors.toList());
        
        return ResultUtils.success(messageVOs);
    }

    /**
     * 获取会话的所有消息（分页）
     */
    @GetMapping("/messages")
    @Operation(summary = "获取会话消息", description = "分页获取指定会话的所有消息")
    public BaseResponse<List<ChatMessageVO>> getSessionMessages(
            @RequestParam String sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("获取会话消息，会话ID: {}, 页码: {}, 大小: {}, 用户ID: {}", 
                sessionId, page, size, BaseContext.getCurrentId());
        
        Integer userId = BaseContext.getCurrentId().intValue();
        
        // 验证会话权限
        ChatSession session = chatSessionService.getSessionById(sessionId, userId);
        if (session == null) {
            return ResultUtils.error("会话不存在或无权限访问");
        }
        
        // 获取分页消息
        List<ChatMessage> messages = chatMessageService.getSessionMessages(sessionId, userId, page, size);
        
        // 转换为VO
        List<ChatMessageVO> messageVOs = messages.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        return ResultUtils.success(messageVOs);
    }

    /**
     * 获取或创建当前会话
     */
    private ChatSession getCurrentOrCreateSession(Integer userId, String sessionId) {
        if (StringUtils.hasText(sessionId)) {
            // 如果指定了会话ID，尝试获取该会话
            ChatSession session = chatSessionService.getSessionById(sessionId, userId);
            if (session != null) {
                // 激活该会话
                chatSessionService.activateSession(sessionId, userId);
                return session;
            }
        }
        
        // 获取当前活跃会话
        ChatSession currentSession = chatSessionService.getCurrentSession(userId);
        if (currentSession == null) {
            // 没有活跃会话，创建新会话
            currentSession = chatSessionService.createNewSession(userId);
        }
        
        return currentSession;
    }

    /**
     * 将ChatMessage实体转换为VO
     */
    private ChatMessageVO convertToVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(message, vo);
        // TODO: 解析metadata JSON字符串为Map
        return vo;
    }
}