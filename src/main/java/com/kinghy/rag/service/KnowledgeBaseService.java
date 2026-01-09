package com.kinghy.rag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinghy.rag.common.KnowledgeBaseType;
import com.kinghy.rag.entity.KnowledgeBase;
import com.kinghy.rag.pojo.dto.CreateKnowledgeBaseDTO;
import com.kinghy.rag.pojo.dto.UpdateKnowledgeBaseDTO;

import java.util.List;

/**
 * 知识库服务接口
 * 提供知识库管理相关的业务逻辑
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
public interface KnowledgeBaseService extends IService<KnowledgeBase> {
    
    /**
     * 创建知识库
     * 
     * @param dto 创建知识库请求DTO
     * @param userId 用户ID
     * @return 创建的知识库
     */
    KnowledgeBase createKnowledgeBase(CreateKnowledgeBaseDTO dto, Long userId);
    
    /**
     * 根据用户ID和类型查询知识库列表
     * 
     * @param userId 用户ID
     * @param type 知识库类型，为null时查询所有类型
     * @return 知识库列表
     */
    List<KnowledgeBase> getUserKnowledgeBases(Long userId, KnowledgeBaseType type);
    
    /**
     * 查询用户可访问的所有知识库（个人知识库 + 公共知识库）
     * 
     * @param userId 用户ID
     * @return 知识库列表
     */
    List<KnowledgeBase> getAccessibleKnowledgeBases(Long userId);
    
    /**
     * 更新知识库信息
     * 
     * @param id 知识库ID
     * @param dto 更新知识库请求DTO
     * @param userId 用户ID
     * @return 更新后的知识库
     */
    KnowledgeBase updateKnowledgeBase(Long id, UpdateKnowledgeBaseDTO dto, Long userId);
    
    /**
     * 删除知识库
     * 
     * @param id 知识库ID
     * @param userId 用户ID
     */
    void deleteKnowledgeBase(Long id, Long userId);
    
    /**
     * 根据关键词搜索用户可访问的知识库
     * 
     * @param keyword 搜索关键词
     * @param userId 用户ID
     * @return 知识库列表
     */
    List<KnowledgeBase> searchKnowledgeBases(String keyword, Long userId);
    
    /**
     * 验证用户是否有权限访问指定知识库
     * 
     * @param knowledgeBaseId 知识库ID
     * @param userId 用户ID
     * @return 知识库信息（如果有权限）
     * @throws com.kinghy.rag.exception.BusinessException 如果无权限或知识库不存在
     */
    KnowledgeBase validateAccess(Long knowledgeBaseId, Long userId);
    
    /**
     * 更新知识库文件数量
     * 
     * @param knowledgeBaseId 知识库ID
     * @param increment 增量（可为负数）
     */
    void updateFileCount(Long knowledgeBaseId, Integer increment);
    
    /**
     * 生成知识库的向量集合名称
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 向量集合名称
     */
    String generateVectorCollectionName(Long knowledgeBaseId);
    
    /**
     * 修复所有知识库的文件计数
     * 同步知识库表中的file_count字段与实际文件数量
     * 
     * @return 修复的知识库数量
     */
    int fixFileCount();
}