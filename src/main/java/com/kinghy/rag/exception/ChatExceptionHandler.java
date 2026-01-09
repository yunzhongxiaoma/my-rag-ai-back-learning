package com.kinghy.rag.exception;

import com.kinghy.rag.common.BaseResponse;
import com.kinghy.rag.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 聊天相关异常处理器
 * 
 * @author yunzhongxiaoma
 */
@RestControllerAdvice
@Slf4j
@Order(1) // 优先级高于全局异常处理器
public class ChatExceptionHandler {

    /**
     * 处理会话未找到异常
     */
    @ExceptionHandler(SessionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public BaseResponse<Void> handleSessionNotFoundException(SessionNotFoundException e) {
        log.warn("Session not found: {}", e.getMessage());
        return ResultUtils.error(40404, e.getMessage());
    }

    /**
     * 处理消息持久化异常
     */
    @ExceptionHandler(MessagePersistenceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<Void> handleMessagePersistenceException(MessagePersistenceException e) {
        log.error("Message persistence error: {}", e.getMessage(), e);
        return ResultUtils.error(50001, "消息保存失败，请稍后重试");
    }

    /**
     * 处理无效会话状态异常
     */
    @ExceptionHandler(InvalidSessionStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleInvalidSessionStateException(InvalidSessionStateException e) {
        log.warn("Invalid session state: {}", e.getMessage());
        return ResultUtils.error(40001, e.getMessage());
    }

    /**
     * 处理用户访问拒绝异常
     */
    @ExceptionHandler(UserAccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public BaseResponse<Void> handleUserAccessDeniedException(UserAccessDeniedException e) {
        log.warn("User access denied: {}", e.getMessage());
        return ResultUtils.error(40301, "访问被拒绝");
    }

    /**
     * 处理通用聊天异常
     */
    @ExceptionHandler(ChatException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<Void> handleChatException(ChatException e) {
        log.error("Chat error [{}]: {}", e.getErrorCode(), e.getMessage(), e);
        return ResultUtils.error(50000, "聊天服务异常，请稍后重试");
    }
}