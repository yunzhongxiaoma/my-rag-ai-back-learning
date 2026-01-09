package com.kinghy.rag.exception;

/**
 * 用户访问拒绝异常
 * 
 * @author yunzhongxiaoma
 */
public class UserAccessDeniedException extends ChatException {

    public UserAccessDeniedException(Integer userId, String resource) {
        super("ACCESS_DENIED", 
              String.format("User %s does not have access to resource: %s", userId, resource));
    }

    public UserAccessDeniedException(Integer userId, String resourceType, String resourceId) {
        super("ACCESS_DENIED", 
              String.format("User %s does not have access to %s: %s", userId, resourceType, resourceId));
    }
}