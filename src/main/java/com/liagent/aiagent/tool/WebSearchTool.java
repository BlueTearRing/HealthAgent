package com.liagent.aiagent.tool;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

/**
 * 网页搜索工具
 */
public class WebSearchTool {

    private String apiKey ;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    private final String API_URL = "https://open.bigmodel.cn/api/paas/v4/web_search";

    @Tool(description = "Some pictures are needed and can be searched online.")
    public String searchWeb(@ToolParam(description = "Search query keyword") String query) {

        // 构建请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("search_query", query);
        params.put("search_engine", "search_std");
        params.put("search_intent", false);
        params.put("count", 10);
        params.put("content_size", "medium");
        params.put("search_recency_filter", "noLimit");

        try {
            // 发送请求
            HttpResponse response = HttpRequest.post(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(params))
                    .timeout(30000)
                    .execute();

            if (response.isOk()) {
                return processResults(response.body());
            } else {
                return "搜索失败: " + response.getStatus() + " - " + response.body();
            }

        } catch (Exception e) {
            return "搜索错误: " + e.getMessage();
        }
    }

    /**
     * 处理搜索结果
     */
    private String processResults(String responseBody) {
        JSONObject json = JSONUtil.parseObj(responseBody);
        StringBuilder result = new StringBuilder();

        // 获取搜索结果
        var searchResults = json.getJSONArray("search_result");

        if (searchResults == null || searchResults.isEmpty()) {
            return "未找到相关结果";
        }

        result.append("找到 ").append(searchResults.size()).append(" 条结果:\n\n");

        for (int i = 0; i < searchResults.size(); i++) {
            JSONObject item = searchResults.getJSONObject(i);

            result.append(i + 1).append(". ").append(item.getStr("title", "")).append("\n");
            result.append("   摘要: ").append(item.getStr("content", "")).append("\n");
            result.append("   来源: ").append(item.getStr("media", "")).append("\n");
            result.append("   链接: ").append(item.getStr("link", "")).append("\n\n");
        }

        return result.toString();
    }
}