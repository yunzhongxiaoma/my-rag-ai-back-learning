package com.kinghy.rag.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

/**
 * 向量存储管理器接口
 * 负责管理不同知识库的向量集合
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
public interface VectorStoreManager {
    
    /**
     * 获取指定知识库的向量存储实例
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 向量存储实例
     */
    VectorStore getVectorStore(Long knowledgeBaseId);
    
    /**
     * 获取指定知识库集合的向量存储实例
     * 
     * @param knowledgeBaseIds 知识库ID列表
     * @return 向量存储实例
     */
    VectorStore getVectorStore(List<Long> knowledgeBaseIds);
    
    /**
     * 为知识库创建向量集合
     * 
     * @param knowledgeBaseId 知识库ID
     * @param collectionName 集合名称
     */
    void createCollection(Long knowledgeBaseId, String collectionName);
    
    /**
     * 删除知识库的向量集合
     * 
     * @param knowledgeBaseId 知识库ID
     * @param collectionName 集合名称
     */
    void deleteCollection(Long knowledgeBaseId, String collectionName);
    
    /**
     * 向指定知识库添加文档向量
     * 
     * @param knowledgeBaseId 知识库ID
     * @param documents 文档列表
     * @return 向量ID列表
     */
    List<String> addDocuments(Long knowledgeBaseId, List<Document> documents);
    
    /**
     * 从指定知识库删除向量
     * 
     * @param knowledgeBaseId 知识库ID
     * @param vectorIds 向量ID列表
     */
    void deleteVectors(Long knowledgeBaseId, List<String> vectorIds);
    
    /**
     * 在指定知识库中搜索相似向量
     * 
     * @param knowledgeBaseIds 知识库ID列表
     * @param searchRequest 搜索请求
     * @return 搜索结果文档列表
     */
    List<Document> similaritySearch(List<Long> knowledgeBaseIds, SearchRequest searchRequest);
    
    /**
     * 检查知识库集合是否存在
     * 
     * @param knowledgeBaseId 知识库ID
     * @param collectionName 集合名称
     * @return 是否存在
     */
    boolean collectionExists(Long knowledgeBaseId, String collectionName);
}