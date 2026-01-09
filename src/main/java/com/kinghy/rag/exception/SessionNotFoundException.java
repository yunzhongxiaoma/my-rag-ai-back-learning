package com.kinghy.rag.exception;

/**
 * 会话未找到异常
 * 
 * @author yunzhongxiaoma
 */
public class SessionNotFoundException extends ChatException {

    public SessionNotFoundException(String sessionId) {
        super("SESSION_NOT_FOUND", "Session not found: " + sessionId);
    }

    public SessionNotFoundException(String sessionId, Integer userId) {
        super("SESSION_NOT_FOUND", 
              String.format("Session not found or access denied: sessionId=%s, userId=%s", sessionId, userId));
    }
}