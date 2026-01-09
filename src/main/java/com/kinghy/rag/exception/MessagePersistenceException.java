package com.kinghy.rag.exception;

/**
 * 消息持久化异常
 * 
 * @author yunzhongxiaoma
 */
public class MessagePersistenceException extends ChatException {

    public MessagePersistenceException(String message) {
        super("MESSAGE_PERSISTENCE_ERROR", message);
    }

    public MessagePersistenceException(String message, Throwable cause) {
        super("MESSAGE_PERSISTENCE_ERROR", message, cause);
    }

    public MessagePersistenceException(String sessionId, String operation, Throwable cause) {
        super("MESSAGE_PERSISTENCE_ERROR", 
              String.format("Failed to %s message for session %s: %s", operation, sessionId, cause.getMessage()), 
              cause);
    }
}