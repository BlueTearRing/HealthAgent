package com.liagent.aiagent.loader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 应用文档加载器 - 用于加载多篇Markdown文档
 * 负责从指定路径读取Markdown文件并将其转换为文档对象
 */
@Component
@Slf4j
public class MedicalDocumentLoader {

    // 资源模式解析器，用于匹配和加载类路径下的资源文件
    private final ResourcePatternResolver resourcePatternResolver;

    /**
     * 构造函数
     * @param resourcePatternResolver 资源模式解析器实例
     */
    public MedicalDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    /**
     * 加载多篇Markdown文档
     * 从classpath:document/目录下读取所有.md文件并解析为文档列表
     * @return 解析后的文档列表，如果加载失败则返回空列表
     */
    public List<Document> loadMarkdowns(){
        List<Document> documents = new ArrayList<>();
        
        // 加载多篇markdown文档
        try {
            // 使用通配符模式匹配document目录下的所有markdown文件
            Resource[] resources = resourcePatternResolver.getResources("classpath:document/*.md");
            
            // 遍历所有匹配到的资源文件
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                
                // 配置Markdown文档读取器
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)  // 水平分隔符创建新文档
                        .withIncludeCodeBlock(false)             // 不包含代码块
                        .withIncludeBlockquote(false)            // 不包含引用块
                        .withAdditionalMetadata("filename", filename)  // 添加文件名到元数据
                        .build();
                
                // 创建Markdown文档读取器并读取文档内容
                MarkdownDocumentReader markdownDocumentReader = new MarkdownDocumentReader(resource, config);
                documents.addAll(markdownDocumentReader.get());
            }

        } catch (IOException e) {
            // 处理文件读取异常，抛出运行时异常
            throw new RuntimeException("加载Markdown文档时发生IO异常", e);
        }
        
        return documents;
    }
}