package com.liagent.aiagent.tool;

import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AllToolManage {

    @Value("${zhipuai.key:test-key}")
    private String ZHIPAIAI_KEY;

    @Value("${search1api.key:test-key}")
    private String SEARCH1API_KEY;


    @Bean
    public ToolCallback[] allTools(){
//         ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        WebSearchTool webSearchTool = new WebSearchTool(ZHIPAIAI_KEY);
        TerminateTool terminateTool = new TerminateTool();
        ImageSearchTool imageSearchTool = new ImageSearchTool(SEARCH1API_KEY);
        return ToolCallbacks.from(
//                 resourceDownloadTool
//                 pdfGenerationTool
                webSearchTool,
                imageSearchTool
//                ,terminateTool

        );
    }
}
