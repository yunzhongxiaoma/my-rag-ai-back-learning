package com.kinghy.rag.common;

/**
 * 知识库类型枚举
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
public enum KnowledgeBaseType {
    /**
     * 个人知识库（私有）
     */
    PERSONAL("个人知识库"),
    
    /**
     * 公共知识库（公开）
     */
    PUBLIC("公共知识库");
    
    private final String description;
    
    KnowledgeBaseType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}