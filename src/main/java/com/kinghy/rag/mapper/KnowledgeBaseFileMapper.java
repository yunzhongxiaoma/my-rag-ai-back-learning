package com.kinghy.rag.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kinghy.rag.entity.KnowledgeBaseFile;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 知识库文件数据访问层接口
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@Mapper
public interface KnowledgeBaseFileMapper extends BaseMapper<KnowledgeBaseFile> {
    
    /**
     * 根据知识库ID查询文件列表
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 文件列表
     */
    List<KnowledgeBaseFile> selectByKnowledgeBaseId(@Param("knowledgeBaseId") Long knowledgeBaseId);
    
    /**
     * 根据知识库ID删除所有文件
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 影响行数
     */
    int deleteByKnowledgeBaseId(@Param("knowledgeBaseId") Long knowledgeBaseId);
    
    /**
     * 统计知识库文件数量
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 文件数量
     */
    int countByKnowledgeBaseId(@Param("knowledgeBaseId") Long knowledgeBaseId);
    
    /**
     * 根据用户ID和文件ID查询文件（用于权限验证）
     * 
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 文件信息
     */
    KnowledgeBaseFile selectByIdAndUserId(@Param("fileId") Long fileId, @Param("userId") Long userId);
}