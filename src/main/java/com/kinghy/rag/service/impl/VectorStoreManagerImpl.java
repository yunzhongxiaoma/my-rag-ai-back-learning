package com.kinghy.rag.service.impl;

import com.kinghy.rag.common.ErrorCode;
import com.kinghy.rag.config.VectorStoreConfig;
import com.kinghy.rag.exception.BusinessException;
import com.kinghy.rag.service.VectorStoreManager;
import io.milvus.client.MilvusServiceClient;
import io.milvus.grpc.DataType;
import io.milvus.param.R;
import io.milvus.param.collection.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 向量存储管理器实现类
 * 基于Milvus实现多知识库的向量存储管理
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@Slf4j
@Service
public class VectorStoreManagerImpl implements VectorStoreManager {
    
    @Autowired
    private MilvusServiceClient milvusClient;
    
    @Autowired
    private VectorStoreConfig.VectorStoreFactory vectorStoreFactory;
    
    @Value("${spring.ai.vectorstore.milvus.embeddingDimension:1536}")
    private int embeddingDimension;
    
    @Value("${spring.ai.vectorstore.milvus.indexType:IVF_FLAT}")
    private String indexType;
    
    // 缓存不同知识库的VectorStore实例
    private final Map<String, VectorStore> vectorStoreCache = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        log.info("VectorStoreManager初始化完成");
    }
    
    @PreDestroy
    public void destroy() {
        // 清理缓存
        vectorStoreCache.clear();
        log.info("VectorStoreManager资源清理完成");
    }
    
    @Override
    public VectorStore getVectorStore(Long knowledgeBaseId) {
        return getVectorStore(Collections.singletonList(knowledgeBaseId));
    }
    
    @Override
    public VectorStore getVectorStore(List<Long> knowledgeBaseIds) {
        if (knowledgeBaseIds == null || knowledgeBaseIds.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "知识库ID列表不能为空");
        }
        
        // 如果只有一个知识库，直接返回对应的VectorStore
        if (knowledgeBaseIds.size() == 1) {
            String collectionName = generateCollectionName(knowledgeBaseIds.get(0));
            return getOrCreateVectorStore(collectionName);
        }
        
        // 多个知识库的情况，创建一个组合的VectorStore
        // 这里我们使用第一个知识库的集合名作为主集合，实际搜索时会在多个集合中进行
        String primaryCollectionName = generateCollectionName(knowledgeBaseIds.get(0));
        return getOrCreateVectorStore(primaryCollectionName);
    }
    
    @Override
    public void createCollection(Long knowledgeBaseId, String collectionName) {
        log.info("为知识库 {} 创建向量集合: {}", knowledgeBaseId, collectionName);
        
        try {
            // 检查集合是否已存在
            if (collectionExists(knowledgeBaseId, collectionName)) {
                log.info("集合 {} 已存在，跳过创建", collectionName);
                return;
            }
            
            // 创建集合字段
            List<FieldType> fieldsSchema = Arrays.asList(
                    FieldType.newBuilder()
                            .withName("id")
                            .withDataType(DataType.VarChar)
                            .withMaxLength(65535)
                            .withPrimaryKey(true)
                            .withAutoID(false)
                            .build(),
                    FieldType.newBuilder()
                            .withName("content")
                            .withDataType(DataType.VarChar)
                            .withMaxLength(65535)
                            .build(),
                    FieldType.newBuilder()
                            .withName("metadata")
                            .withDataType(DataType.VarChar)
                            .withMaxLength(65535)
                            .build(),
                    FieldType.newBuilder()
                            .withName("embedding")
                            .withDataType(DataType.FloatVector)
                            .withDimension(embeddingDimension)
                            .build()
            );
            
            // 创建集合
            CreateCollectionParam createCollectionParam = CreateCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .withDescription("知识库 " + knowledgeBaseId + " 的向量集合")
                    .withSchema(CollectionSchemaParam.newBuilder()
                            .withFieldTypes(fieldsSchema)
                            .build())
                    .build();
            
            R<?> response = milvusClient.createCollection(createCollectionParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new BusinessException(ErrorCode.VECTOR_STORE_ERROR, 
                        "创建向量集合失败: " + response.getMessage());
            }
            
            log.info("向量集合 {} 创建成功", collectionName);
            
        } catch (Exception e) {
            log.error("创建向量集合失败", e);
            throw new BusinessException(ErrorCode.VECTOR_STORE_ERROR, 
                    "创建向量集合失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteCollection(Long knowledgeBaseId, String collectionName) {
        log.info("删除知识库 {} 的向量集合: {}", knowledgeBaseId, collectionName);
        
        try {
            // 检查集合是否存在
            if (!collectionExists(knowledgeBaseId, collectionName)) {
                log.info("集合 {} 不存在，跳过删除", collectionName);
                return;
            }
            
            // 删除集合
            DropCollectionParam dropCollectionParam = DropCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();
            
            R<?> response = milvusClient.dropCollection(dropCollectionParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                throw new BusinessException(ErrorCode.VECTOR_STORE_ERROR, 
                        "删除向量集合失败: " + response.getMessage());
            }
            
            // 从缓存中移除
            vectorStoreCache.remove(collectionName);
            
            log.info("向量集合 {} 删除成功", collectionName);
            
        } catch (Exception e) {
            log.error("删除向量集合失败", e);
            throw new BusinessException(ErrorCode.VECTOR_STORE_ERROR, 
                    "删除向量集合失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<String> addDocuments(Long knowledgeBaseId, List<Document> documents) {
        log.info("向知识库 {} 添加 {} 个文档向量", knowledgeBaseId, documents.size());
        
        try {
            VectorStore vectorStore = getVectorStore(knowledgeBaseId);
            vectorStore.add(documents);
            
            // 返回文档ID列表
            List<String> documentIds = new ArrayList<>();
            for (Document document : documents) {
                documentIds.add(document.getId());
            }
            
            log.info("成功向知识库 {} 添加 {} 个文档向量", knowledgeBaseId, documents.size());
            return documentIds;
            
        } catch (Exception e) {
            log.error("添加文档向量失败", e);
            throw new BusinessException(ErrorCode.VECTOR_STORE_ERROR, 
                    "添加文档向量失败: " + e.getMessage());
        }
    }
    
    @Override
    public void deleteVectors(Long knowledgeBaseId, List<String> vectorIds) {
        log.info("从知识库 {} 删除 {} 个向量", knowledgeBaseId, vectorIds.size());
        
        try {
            VectorStore vectorStore = getVectorStore(knowledgeBaseId);
            vectorStore.delete(vectorIds);
            
            log.info("成功从知识库 {} 删除 {} 个向量", knowledgeBaseId, vectorIds.size());
            
        } catch (Exception e) {
            log.error("删除向量失败", e);
            throw new BusinessException(ErrorCode.VECTOR_STORE_ERROR, 
                    "删除向量失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<Document> similaritySearch(List<Long> knowledgeBaseIds, SearchRequest searchRequest) {
        log.info("在知识库 {} 中进行相似性搜索", knowledgeBaseIds);
        
        try {
            List<Document> allResults = new ArrayList<>();
            
            // 在每个知识库中进行搜索
            for (Long knowledgeBaseId : knowledgeBaseIds) {
                try {
                    VectorStore vectorStore = getVectorStore(knowledgeBaseId);
                    List<Document> results = vectorStore.similaritySearch(searchRequest);
                    allResults.addAll(results);
                } catch (Exception e) {
                    log.warn("在知识库 {} 中搜索失败: {}", knowledgeBaseId, e.getMessage());
                    // 继续搜索其他知识库
                }
            }
            
            // 按相似度排序并限制结果数量
            allResults.sort((d1, d2) -> {
                Float score1 = d1.getMetadata().get("distance") != null ? 
                        Float.parseFloat(d1.getMetadata().get("distance").toString()) : 0f;
                Float score2 = d2.getMetadata().get("distance") != null ? 
                        Float.parseFloat(d2.getMetadata().get("distance").toString()) : 0f;
                return Float.compare(score1, score2);
            });
            
            // 限制返回结果数量
            int topK = searchRequest.getTopK();
            if (allResults.size() > topK) {
                allResults = allResults.subList(0, topK);
            }
            
            log.info("相似性搜索完成，返回 {} 个结果", allResults.size());
            return allResults;
            
        } catch (Exception e) {
            log.error("相似性搜索失败", e);
            throw new BusinessException(ErrorCode.VECTOR_STORE_ERROR, 
                    "相似性搜索失败: " + e.getMessage());
        }
    }
    
    @Override
    public boolean collectionExists(Long knowledgeBaseId, String collectionName) {
        try {
            HasCollectionParam hasCollectionParam = HasCollectionParam.newBuilder()
                    .withCollectionName(collectionName)
                    .build();
            
            R<Boolean> response = milvusClient.hasCollection(hasCollectionParam);
            if (response.getStatus() != R.Status.Success.getCode()) {
                log.warn("检查集合存在性失败: {}", response.getMessage());
                return false;
            }
            
            return response.getData();
            
        } catch (Exception e) {
            log.error("检查集合存在性时发生异常", e);
            return false;
        }
    }
    
    /**
     * 生成集合名称
     * 
     * @param knowledgeBaseId 知识库ID
     * @return 集合名称
     */
    private String generateCollectionName(Long knowledgeBaseId) {
        return "kb_" + knowledgeBaseId;
    }
    
    /**
     * 获取或创建VectorStore实例
     * 
     * @param collectionName 集合名称
     * @return VectorStore实例
     */
    private VectorStore getOrCreateVectorStore(String collectionName) {
        return vectorStoreCache.computeIfAbsent(collectionName, name -> {
            log.info("创建新的VectorStore实例，集合名称: {}", name);
            VectorStore vectorStore = vectorStoreFactory.createVectorStore(name);
            if (vectorStore instanceof MilvusVectorStore) {
                return (MilvusVectorStore) vectorStore;
            }
            // 如果工厂返回null或其他类型，记录警告并返回null
            log.warn("VectorStoreFactory返回了非MilvusVectorStore类型或null，集合: {}", name);
            return null;
        });
    }
}