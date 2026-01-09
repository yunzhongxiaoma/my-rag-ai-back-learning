package com.kinghy.rag.controller;

import com.kinghy.rag.common.ApplicationConstant;
import com.kinghy.rag.common.BaseResponse;
import com.kinghy.rag.common.ResultUtils;
import com.kinghy.rag.context.BaseContext;
import com.kinghy.rag.entity.ChatSession;
import com.kinghy.rag.entity.ChatMessage;
import com.kinghy.rag.pojo.vo.ChatSessionVO;
import com.kinghy.rag.pojo.vo.ChatMessageVO;
import com.kinghy.rag.pojo.vo.CursorPageResult;
import com.kinghy.rag.pojo.vo.PageResult;
import com.kinghy.rag.service.ChatSessionService;
import com.kinghy.rag.service.ChatMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天会话管理控制器
 * 
 * @author yunzhongxiaoma
 */
@Tag(name = "ChatSessionController", description = "聊天会话管理接口")
@Slf4j
@RestController
@RequestMapping(ApplicationConstant.API_VERSION + "/chat/session")
public class ChatSessionController {

    @Autowired
    private ChatSessionService chatSessionService;

    @Autowired
    private ChatMessageService chatMessageService;

    /**
     * 获取当前用户的活跃会话
     */
    @GetMapping("/current")
    @Operation(summary = "获取当前会话", description = "获取当前用户的活跃会话")
    public BaseResponse<ChatSessionVO> getCurrentSession() {
        log.info("获取当前用户会话，用户ID: {}", BaseContext.getCurrentId());
        
        Integer userId = BaseContext.getCurrentId().intValue();
        ChatSession currentSession = chatSessionService.getCurrentSession(userId);
        
        if (currentSession == null) {
            // 如果没有当前会话，创建一个新的
            currentSession = chatSessionService.createNewSession(userId);
        }
        
        ChatSessionVO sessionVO = convertToVO(currentSession);
        
        // 获取最近的几条消息作为预览
        List<ChatMessage> recentMessages = chatMessageService.getRecentMessages(
                currentSession.getSessionId(), userId, 3);
        sessionVO.setRecentMessages(convertMessagesToVO(recentMessages));
        
        return ResultUtils.success(sessionVO);
    }

    /**
     * 创建新的聊天会话
     */
    @PostMapping("/new")
    @Operation(summary = "创建新会话", description = "为当前用户创建新的聊天会话")
    public BaseResponse<ChatSessionVO> createNewSession() {
        log.info("创建新会话，用户ID: {}", BaseContext.getCurrentId());
        
        Integer userId = BaseContext.getCurrentId().intValue();
        ChatSession newSession = chatSessionService.createNewSession(userId);
        
        ChatSessionVO sessionVO = convertToVO(newSession);
        sessionVO.setRecentMessages(List.of()); // 新会话没有消息
        
        return ResultUtils.success(sessionVO);
    }

    /**
     * 获取用户的会话列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取用户会话列表", description = "分页获取当前用户的所有会话")
    public BaseResponse<List<ChatSessionVO>> getUserSessions(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "false") boolean withTotal) {
        
        log.info("获取用户会话列表，用户ID: {}, 页码: {}, 大小: {}, 包含总数: {}", 
                BaseContext.getCurrentId(), page, size, withTotal);
        
        Integer userId = BaseContext.getCurrentId().intValue();
        
        if (withTotal) {
            // 返回带总数的分页结果
            PageResult<ChatSession> pageResult = chatSessionService.getUserSessionsWithTotal(userId, page, size);
            
            List<ChatSessionVO> sessionVOs = pageResult.getRecords().stream()
                    .map(session -> {
                        ChatSessionVO vo = convertToVO(session);
                        // 为每个会话获取最近的消息预览
                        List<ChatMessage> recentMessages = chatMessageService.getRecentMessages(
                                session.getSessionId(), userId, 2);
                        vo.setRecentMessages(convertMessagesToVO(recentMessages));
                        return vo;
                    })
                    .collect(Collectors.toList());
            
            // 这里简化返回，实际项目中可能需要返回完整的分页信息
            return ResultUtils.success(sessionVOs);
        } else {
            // 返回简单列表
            List<ChatSession> sessions = chatSessionService.getUserSessions(userId, page, size);
            
            List<ChatSessionVO> sessionVOs = sessions.stream()
                    .map(session -> {
                        ChatSessionVO vo = convertToVO(session);
                        // 为每个会话获取最近的消息预览
                        List<ChatMessage> recentMessages = chatMessageService.getRecentMessages(
                                session.getSessionId(), userId, 2);
                        vo.setRecentMessages(convertMessagesToVO(recentMessages));
                        return vo;
                    })
                    .collect(Collectors.toList());
            
            return ResultUtils.success(sessionVOs);
        }
    }

    /**
     * 获取用户的会话列表（游标分页）
     */
    @GetMapping("/list/cursor")
    @Operation(summary = "获取用户会话列表（游标分页）", description = "使用游标分页获取当前用户的所有会话")
    public BaseResponse<CursorPageResult<ChatSessionVO>> getUserSessionsByCursor(
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("获取用户会话列表（游标分页），用户ID: {}, 游标: {}, 大小: {}", 
                BaseContext.getCurrentId(), cursor, size);
        
        Integer userId = BaseContext.getCurrentId().intValue();
        CursorPageResult<ChatSession> pageResult = chatSessionService.getUserSessionsByCursor(userId, cursor, size);
        
        List<ChatSessionVO> sessionVOs = pageResult.getRecords().stream()
                .map(session -> {
                    ChatSessionVO vo = convertToVO(session);
                    // 为每个会话获取最近的消息预览
                    List<ChatMessage> recentMessages = chatMessageService.getRecentMessages(
                            session.getSessionId(), userId, 2);
                    vo.setRecentMessages(convertMessagesToVO(recentMessages));
                    return vo;
                })
                .collect(Collectors.toList());
        
        CursorPageResult<ChatSessionVO> result = CursorPageResult.of(
                sessionVOs, 
                pageResult.getCursor(), 
                pageResult.getNextCursor(), 
                pageResult.getSize(), 
                pageResult.isHasNext()
        );
        
        return ResultUtils.success(result);
    }

    /**
     * 获取指定会话详情
     */
    @GetMapping("/{sessionId}")
    @Operation(summary = "获取指定会话详情", description = "根据会话ID获取会话详细信息")
    public BaseResponse<ChatSessionVO> getSession(@PathVariable String sessionId) {
        log.info("获取会话详情，会话ID: {}, 用户ID: {}", sessionId, BaseContext.getCurrentId());
        
        Integer userId = BaseContext.getCurrentId().intValue();
        ChatSession session = chatSessionService.getSessionById(sessionId, userId);
        
        if (session == null) {
            return ResultUtils.error("会话不存在或无权限访问");
        }
        
        ChatSessionVO sessionVO = convertToVO(session);
        
        // 获取最近的消息作为预览
        List<ChatMessage> recentMessages = chatMessageService.getRecentMessages(
                sessionId, userId, 5);
        sessionVO.setRecentMessages(convertMessagesToVO(recentMessages));
        
        return ResultUtils.success(sessionVO);
    }

    /**
     * 激活指定会话
     */
    @PostMapping("/{sessionId}/activate")
    @Operation(summary = "激活会话", description = "将指定会话设为当前活跃会话")
    public BaseResponse<String> activateSession(@PathVariable String sessionId) {
        log.info("激活会话，会话ID: {}, 用户ID: {}", sessionId, BaseContext.getCurrentId());
        
        Integer userId = BaseContext.getCurrentId().intValue();
        
        // 验证会话是否存在且属于当前用户
        ChatSession session = chatSessionService.getSessionById(sessionId, userId);
        if (session == null) {
            return ResultUtils.error("会话不存在或无权限访问");
        }
        
        chatSessionService.activateSession(sessionId, userId);
        
        return ResultUtils.success("会话激活成功");
    }

    /**
     * 更新会话标题
     */
    @PutMapping("/{sessionId}/title")
    @Operation(summary = "更新会话标题", description = "更新指定会话的标题")
    public BaseResponse<String> updateSessionTitle(
            @PathVariable String sessionId,
            @RequestParam String title) {
        
        log.info("更新会话标题，会话ID: {}, 新标题: {}, 用户ID: {}", 
                sessionId, title, BaseContext.getCurrentId());
        
        Integer userId = BaseContext.getCurrentId().intValue();
        
        // 验证会话是否存在且属于当前用户
        ChatSession session = chatSessionService.getSessionById(sessionId, userId);
        if (session == null) {
            return ResultUtils.error("会话不存在或无权限访问");
        }
        
        chatSessionService.updateSessionTitle(sessionId, userId, title);
        
        return ResultUtils.success("会话标题更新成功");
    }

    /**
     * 结束会话
     */
    @PostMapping("/{sessionId}/end")
    @Operation(summary = "结束会话", description = "结束指定的聊天会话")
    public BaseResponse<String> endSession(@PathVariable String sessionId) {
        log.info("结束会话，会话ID: {}, 用户ID: {}", sessionId, BaseContext.getCurrentId());
        
        Integer userId = BaseContext.getCurrentId().intValue();
        
        // 验证会话是否存在且属于当前用户
        ChatSession session = chatSessionService.getSessionById(sessionId, userId);
        if (session == null) {
            return ResultUtils.error("会话不存在或无权限访问");
        }
        
        chatSessionService.endSession(sessionId, userId);
        
        return ResultUtils.success("会话已结束");
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/{sessionId}")
    @Operation(summary = "删除会话", description = "删除指定的聊天会话及其所有消息")
    public BaseResponse<String> deleteSession(@PathVariable String sessionId) {
        log.info("删除会话，会话ID: {}, 用户ID: {}", sessionId, BaseContext.getCurrentId());
        
        Integer userId = BaseContext.getCurrentId().intValue();
        
        // 验证会话是否存在且属于当前用户
        ChatSession session = chatSessionService.getSessionById(sessionId, userId);
        if (session == null) {
            return ResultUtils.error("会话不存在或无权限访问");
        }
        
        chatSessionService.deleteSession(sessionId, userId);
        
        return ResultUtils.success("会话删除成功");
    }

    /**
     * 将ChatSession实体转换为VO
     */
    private ChatSessionVO convertToVO(ChatSession session) {
        ChatSessionVO vo = new ChatSessionVO();
        BeanUtils.copyProperties(session, vo);
        return vo;
    }

    /**
     * 将ChatMessage列表转换为VO列表
     */
    private List<ChatMessageVO> convertMessagesToVO(List<ChatMessage> messages) {
        return messages.stream()
                .map(this::convertMessageToVO)
                .collect(Collectors.toList());
    }

    /**
     * 将ChatMessage实体转换为VO
     */
    private ChatMessageVO convertMessageToVO(ChatMessage message) {
        ChatMessageVO vo = new ChatMessageVO();
        BeanUtils.copyProperties(message, vo);
        // TODO: 解析metadata JSON字符串为Map
        return vo;
    }

    /**
     * 搜索用户的会话
     */
    @GetMapping("/search")
    @Operation(summary = "搜索用户会话", description = "根据标题关键词搜索用户的会话")
    public BaseResponse<List<ChatSessionVO>> searchUserSessions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        log.info("搜索用户会话，用户ID: {}, 关键词: {}, 页码: {}, 大小: {}", 
                BaseContext.getCurrentId(), keyword, page, size);
        
        Integer userId = BaseContext.getCurrentId().intValue();
        
        // 这里可以扩展为使用专门的搜索方法
        List<ChatSession> sessions = chatSessionService.getUserSessions(userId, page, size);
        
        // 简单的关键词过滤（实际项目中可能需要更复杂的搜索逻辑）
        List<ChatSession> filteredSessions = sessions.stream()
                .filter(session -> session.getTitle() != null && 
                                 session.getTitle().toLowerCase().contains(keyword.toLowerCase()))
                .collect(Collectors.toList());
        
        List<ChatSessionVO> sessionVOs = filteredSessions.stream()
                .map(session -> {
                    ChatSessionVO vo = convertToVO(session);
                    // 为每个会话获取最近的消息预览
                    List<ChatMessage> recentMessages = chatMessageService.getRecentMessages(
                            session.getSessionId(), userId, 2);
                    vo.setRecentMessages(convertMessagesToVO(recentMessages));
                    return vo;
                })
                .collect(Collectors.toList());
        
        return ResultUtils.success(sessionVOs);
    }

    /**
     * 获取会话的完整消息列表
     */
    @GetMapping("/{sessionId}/messages")
    @Operation(summary = "获取会话消息列表", description = "获取指定会话的完整消息列表")
    public BaseResponse<List<ChatMessageVO>> getSessionMessages(
            @PathVariable String sessionId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        log.info("获取会话消息列表，会话ID: {}, 用户ID: {}, 页码: {}, 大小: {}", 
                sessionId, BaseContext.getCurrentId(), page, size);
        
        Integer userId = BaseContext.getCurrentId().intValue();
        
        // 验证会话权限
        ChatSession session = chatSessionService.getSessionById(sessionId, userId);
        if (session == null) {
            return ResultUtils.error("会话不存在或无权限访问");
        }
        
        // 获取消息列表
        List<ChatMessage> messages = chatMessageService.getSessionMessages(sessionId, userId, page, size);
        
        List<ChatMessageVO> messageVOs = messages.stream()
                .map(this::convertMessageToVO)
                .collect(Collectors.toList());
        
        return ResultUtils.success(messageVOs);
    }
}