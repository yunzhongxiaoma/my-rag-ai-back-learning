package com.kinghy.rag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kinghy.rag.entity.KnowledgeBaseFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库文件服务接口
 * 提供知识库文件管理相关的业务逻辑
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
public interface KnowledgeBaseFileService extends IService<KnowledgeBaseFile> {
    
    /**
     * 上传文件到指定知识库
     * 
     * @param knowledgeBaseId 知识库ID
     * @param file 上传的文件
     * @param userId 用户ID
     * @return 上传后的文件信息
     */
    KnowledgeBaseFile uploadFile(Long knowledgeBaseId, MultipartFile file, Long userId);
    
    /**
     * 批量上传文件到指定知识库
     * 
     * @param knowledgeBaseId 知识库ID
     * @param files 上传的文件列表
     * @param userId 用户ID
     * @return 上传后的文件信息列表
     */
    List<KnowledgeBaseFile> uploadFiles(Long knowledgeBaseId, List<MultipartFile> files, Long userId);
    
    /**
     * 删除知识库文件
     * 
     * @param fileId 文件ID
     * @param userId 用户ID
     */
    void deleteFile(Long fileId, Long userId);
    
    /**
     * 批量删除知识库文件
     * 
     * @param fileIds 文件ID列表
     * @param userId 用户ID
     */
    void deleteFiles(List<Long> fileIds, Long userId);
    
    /**
     * 获取知识库文件列表
     * 
     * @param knowledgeBaseId 知识库ID
     * @param userId 用户ID
     * @return 文件列表
     */
    List<KnowledgeBaseFile> getKnowledgeBaseFiles(Long knowledgeBaseId, Long userId);
    
    /**
     * 根据知识库ID删除所有文件
     * 
     * @param knowledgeBaseId 知识库ID
     * @param userId 用户ID
     */
    void deleteAllFilesByKnowledgeBaseId(Long knowledgeBaseId, Long userId);
    
    /**
     * 验证用户是否有权限访问指定文件
     * 
     * @param fileId 文件ID
     * @param userId 用户ID
     * @return 文件信息（如果有权限）
     * @throws com.kinghy.rag.exception.BusinessException 如果无权限或文件不存在
     */
    KnowledgeBaseFile validateFileAccess(Long fileId, Long userId);
    
    /**
     * 统计知识库文件数量
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 文件数量
     */
    int countFilesByKnowledgeBaseId(Long knowledgeBaseId);
}