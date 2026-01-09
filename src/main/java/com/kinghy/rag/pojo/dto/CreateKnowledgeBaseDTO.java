package com.kinghy.rag.pojo.dto;

import com.kinghy.rag.common.KnowledgeBaseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 创建知识库请求DTO
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateKnowledgeBaseDTO implements Serializable {
    
    /**
     * 知识库显示名称
     */
    private String displayName;
    
    /**
     * 知识库类型
     */
    private KnowledgeBaseType type;
    
    /**
     * 知识库描述
     */
    private String description;
}