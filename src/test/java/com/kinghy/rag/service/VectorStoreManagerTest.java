package com.kinghy.rag.service;

import com.kinghy.rag.config.VectorStoreConfig;
import com.kinghy.rag.service.impl.VectorStoreManagerImpl;
import io.milvus.client.MilvusServiceClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 向量存储管理器测试类
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class VectorStoreManagerTest {
    
    @Mock
    private MilvusServiceClient milvusClient;
    
    @Mock
    private VectorStoreConfig.VectorStoreFactory vectorStoreFactory;
    
    @Mock
    private VectorStore mockVectorStore;
    
    @InjectMocks
    private VectorStoreManagerImpl vectorStoreManager;
    
    private Long testKnowledgeBaseId;
    
    @BeforeEach
    void setUp() {
        testKnowledgeBaseId = 1L;
    }
    
    @Test
    void testGetVectorStore_SingleKnowledgeBase() {
        // 准备测试数据
        when(vectorStoreFactory.createVectorStore(any())).thenReturn(mockVectorStore);
        
        // 执行测试
        VectorStore result = vectorStoreManager.getVectorStore(testKnowledgeBaseId);
        
        // 验证结果
        assertNotNull(result);
        verify(vectorStoreFactory, times(1)).createVectorStore("kb_1");
    }
    
    @Test
    void testGetVectorStore_MultipleKnowledgeBases() {
        // 准备测试数据
        List<Long> knowledgeBaseIds = Arrays.asList(1L, 2L, 3L);
        when(vectorStoreFactory.createVectorStore(any())).thenReturn(mockVectorStore);
        
        // 执行测试
        VectorStore result = vectorStoreManager.getVectorStore(knowledgeBaseIds);
        
        // 验证结果
        assertNotNull(result);
        verify(vectorStoreFactory, times(1)).createVectorStore("kb_1");
    }
    
    @Test
    void testAddDocuments() {
        // 准备测试数据
        Document doc1 = new Document("test content 1");
        Document doc2 = new Document("test content 2");
        List<Document> documents = Arrays.asList(doc1, doc2);
        
        when(vectorStoreFactory.createVectorStore(any())).thenReturn(mockVectorStore);
        doNothing().when(mockVectorStore).add(any());
        
        // 执行测试
        List<String> result = vectorStoreManager.addDocuments(testKnowledgeBaseId, documents);
        
        // 验证结果
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(mockVectorStore, times(1)).add(documents);
    }
    
    @Test
    void testDeleteVectors() {
        // 准备测试数据
        List<String> vectorIds = Arrays.asList("vector1", "vector2");
        
        when(vectorStoreFactory.createVectorStore(any())).thenReturn(mockVectorStore);
        doNothing().when(mockVectorStore).delete(anyList());
        
        // 执行测试
        assertDoesNotThrow(() -> {
            vectorStoreManager.deleteVectors(testKnowledgeBaseId, vectorIds);
        });
        
        // 验证结果
        verify(mockVectorStore, times(1)).delete(vectorIds);
    }
    
    @Test
    void testGenerateCollectionName() {
        // 使用反射访问私有方法进行测试
        // 这里我们通过公共方法间接测试集合名称生成逻辑
        when(vectorStoreFactory.createVectorStore("kb_123")).thenReturn(mockVectorStore);
        
        // 执行测试
        VectorStore result = vectorStoreManager.getVectorStore(123L);
        
        // 验证结果
        assertNotNull(result);
        verify(vectorStoreFactory, times(1)).createVectorStore("kb_123");
    }
}