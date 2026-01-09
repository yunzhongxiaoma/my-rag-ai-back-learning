package com.kinghy.rag.exception;

/**
 * 聊天相关异常基类
 * 
 * @author yunzhongxiaoma
 */
public class ChatException extends RuntimeException {

    private final String errorCode;

    public ChatException(String message) {
        super(message);
        this.errorCode = "CHAT_ERROR";
    }

    public ChatException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ChatException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "CHAT_ERROR";
    }

    public ChatException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}