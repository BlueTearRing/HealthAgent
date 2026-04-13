package com.liagent.aiagent.advisor;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 云知识库配置
 */
@Configuration
@Slf4j
public class DietPlanRagCloudAdvisorConfig {

    @Value("${spring.ai.dashscope.api-key:test-key}")
    private String dashscopeApiKey;

    @Value("${dashscope.rag.enabled:false}")
    private boolean ragEnabled;

    private final String KNOWLEDGE_INDEX = "饮食规划";

    @Bean
    public Advisor dietPlanRagCloudAdvisor() {
        if (!ragEnabled || "test-key".equals(dashscopeApiKey) || dashscopeApiKey == null || dashscopeApiKey.isBlank()) {
            log.warn("云知识库 RAG advisor 未启用（dashscope.rag.enabled=false 或 API Key 未配置）");
            return null;
        }
        try {
            // 初始化DashScope API客户端
            DashScopeApi dashScopeApi = new DashScopeApi(dashscopeApiKey);

            // 创建文档检索器，配置使用指定的知识索引
            DocumentRetriever documentRetriever = new DashScopeDocumentRetriever(dashScopeApi,
                    DashScopeDocumentRetrieverOptions.builder()
                            .withIndexName(KNOWLEDGE_INDEX)  // 设置知识索引名称
                            .build());

            // 构建并返回检索增强生成顾问
            return RetrievalAugmentationAdvisor.builder()
                    .documentRetriever(documentRetriever)  // 注入文档检索器
                    .build();
        } catch (Exception e) {
            log.warn("云知识库 RAG advisor 初始化失败，跳过: {}", e.getMessage());
            return null;
        }
    }

}
