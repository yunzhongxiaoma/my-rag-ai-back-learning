package com.kinghy.rag.pojo.dto;

import com.kinghy.rag.common.KnowledgeBaseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 知识库查询请求DTO
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseQueryDTO implements Serializable {
    
    /**
     * 知识库类型
     */
    private KnowledgeBaseType type;
    
    /**
     * 搜索关键词
     */
    private String keyword;
}