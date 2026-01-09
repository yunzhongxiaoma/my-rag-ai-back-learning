package com.kinghy.rag.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.kinghy.rag.common.KnowledgeBaseType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库实体类
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@TableName(value = "tb_knowledge_base")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBase {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 知识库唯一标识名
     */
    @TableField("name")
    private String name;
    
    /**
     * 知识库显示名称
     */
    @TableField("display_name")
    private String displayName;
    
    /**
     * 知识库类型
     */
    @TableField("type")
    private KnowledgeBaseType type;
    
    /**
     * 创建者用户ID
     */
    @TableField("creator_id")
    private Integer creatorId;
    
    /**
     * 知识库描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 文件数量
     */
    @TableField("file_count")
    private Integer fileCount;
    
    /**
     * Milvus集合名称
     */
    @TableField("vector_collection_name")
    private String vectorCollectionName;
    
    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}