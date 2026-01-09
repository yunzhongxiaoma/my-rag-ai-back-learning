package com.kinghy.rag.config;

import io.milvus.client.MilvusServiceClient;
import io.milvus.param.ConnectParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 向量存储配置类
 * 配置Milvus向量数据库相关的Bean
 * 
 * @author yunzhongxiaoma
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class VectorStoreConfig {
    
    @Value("${spring.ai.vectorstore.milvus.client.host:localhost}")
    private String milvusHost;
    
    @Value("${spring.ai.vectorstore.milvus.client.port:19530}")
    private int milvusPort;
    
    @Value("${spring.ai.vectorstore.milvus.databaseName:default}")
    private String databaseName;
    
    /**
     * 创建Milvus客户端
     * 
     * @return MilvusServiceClient实例
     */
    @Bean
    public MilvusServiceClient milvusServiceClient() {
        log.info("创建Milvus客户端，主机: {}, 端口: {}, 数据库: {}", milvusHost, milvusPort, databaseName);
        
        ConnectParam.Builder connectBuilder = ConnectParam.newBuilder()
                .withHost(milvusHost)
                .withPort(milvusPort)
                .withDatabaseName(databaseName);
        
        return new MilvusServiceClient(connectBuilder.build());
    }
    
    /**
     * 创建向量存储工厂
     * 用于动态创建不同知识库的向量存储实例
     * 
     * @param milvusServiceClient Milvus客户端
     * @param embeddingModel 嵌入模型
     * @return VectorStoreFactory实例
     */
    @Bean
    public VectorStoreFactory vectorStoreFactory(MilvusServiceClient milvusServiceClient,
                                                EmbeddingModel embeddingModel) {
        return new VectorStoreFactory(milvusServiceClient, embeddingModel);
    }
    
    /**
     * 向量存储工厂类
     * 用于动态创建不同集合的向量存储实例
     */
    public static class VectorStoreFactory {
        private final MilvusServiceClient milvusServiceClient;
        private final EmbeddingModel embeddingModel;
        
        public VectorStoreFactory(MilvusServiceClient milvusServiceClient,
                                 EmbeddingModel embeddingModel) {
            this.milvusServiceClient = milvusServiceClient;
            this.embeddingModel = embeddingModel;
        }
        
        /**
         * 创建指定集合名称的向量存储实例
         * 
         * @param collectionName 集合名称
         * @return VectorStore实例（返回接口类型以避免具体实现的构造函数问题）
         */
        public VectorStore createVectorStore(String collectionName) {
            log.info("创建向量存储实例，集合名称: {}", collectionName);
            
            // 这里暂时返回null，实际实现需要根据Spring AI的具体版本调整
            // 或者使用Spring AI的自动配置机制
            log.warn("VectorStore创建暂未实现，需要根据Spring AI版本调整");
            return null;
        }
    }
}