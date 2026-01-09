package com.kinghy.rag.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 知识库文件实体类
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@TableName(value = "tb_knowledge_base_file")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeBaseFile {
    
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 知识库ID
     */
    @TableField("knowledge_base_id")
    private Long knowledgeBaseId;
    
    /**
     * 存储文件名
     */
    @TableField("file_name")
    private String fileName;
    
    /**
     * 原始文件名
     */
    @TableField("original_name")
    private String originalName;
    
    /**
     * 文件URL
     */
    @TableField("file_url")
    private String fileUrl;
    
    /**
     * 文件大小(字节)
     */
    @TableField("file_size")
    private Long fileSize;
    
    /**
     * 文件类型
     */
    @TableField("file_type")
    private String fileType;
    
    /**
     * 向量ID列表(JSON格式)
     */
    @TableField("vector_ids")
    private String vectorIds;
    
    /**
     * 上传用户ID
     */
    @TableField("upload_user_id")
    private Integer uploadUserId;
    
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