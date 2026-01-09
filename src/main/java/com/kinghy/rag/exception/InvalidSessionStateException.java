package com.kinghy.rag.exception;

/**
 * 无效会话状态异常
 * 
 * @author yunzhongxiaoma
 */
public class InvalidSessionStateException extends ChatException {

    public InvalidSessionStateException(String sessionId, String operation) {
        super("INVALID_SESSION_STATE", 
              String.format("Cannot perform operation '%s' on session %s: invalid session state", operation, sessionId));
    }

    public InvalidSessionStateException(String sessionId, String currentState, String requiredState) {
        super("INVALID_SESSION_STATE", 
              String.format("Session %s is in state '%s', but operation requires state '%s'", 
                          sessionId, currentState, requiredState));
    }
}