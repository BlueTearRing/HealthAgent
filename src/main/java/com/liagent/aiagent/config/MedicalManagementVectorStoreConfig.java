package com.liagent.aiagent.config;

import com.liagent.aiagent.loader.MedicalDocumentLoader;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 初始化向量数据库bean
 */
@Configuration
@Slf4j
public class MedicalManagementVectorStoreConfig {

    @Resource
    private MedicalDocumentLoader documentLoader;

    @Value("${spring.ai.dashscope.api-key:test-key}")
    private String dashscopeApiKey;

    @Bean
    VectorStore MedicalManagementVectorStore(EmbeddingModel embeddingModel) {
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(embeddingModel).build();
        if (dashscopeApiKey == null || dashscopeApiKey.isBlank() || "test-key".equals(dashscopeApiKey)) {
            log.warn("DashScope API key is not configured with a real value, starting medical vector store without preloaded documents");
            return vectorStore;
        }
        try {
            vectorStore.add(documentLoader.loadMarkdowns());
        } catch (Exception e) {
            log.warn("Failed to initialize medical vector store, continuing with an empty store", e);
        }
        return vectorStore;
    }
}
