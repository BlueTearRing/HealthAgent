package com.liagent.aiagent.tool;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 图片搜索工具
 */
public class ImageSearchTool {

    private String apiKey ;

    public ImageSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    private final String API_URL = "https://api.search1api.com/search";

    @Tool(description = "Search for images from search engine")
    public String searchImages(@ToolParam(description = "Search query keyword") String query) {
        
        // 构建请求参数
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("search_service", "google");
        params.put("max_results", 5);
        params.put("crawl_results", 0);
        params.put("image", true);
        params.put("language", "");

        try {
            // 发送请求
            HttpResponse response = HttpRequest.post(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(params))
                    .timeout(30000)
                    .execute();

            if (response.isOk()) {
                return processImageResults(response.body());
            } else {
                return "图片搜索失败: " + response.getStatus() + " - " + response.body();
            }

        } catch (Exception e) {
            return "图片搜索错误: " + e.getMessage();
        }
    }

    /**
     * 处理图片搜索结果
     */
    private String processImageResults(String responseBody) {
        JSONObject json = JSONUtil.parseObj(responseBody);
        StringBuilder result = new StringBuilder();
        
        // 获取普通搜索结果
        var searchResults = json.getJSONArray("results");
        // 获取图片结果
        var imageResults = json.getJSONArray("images");
        
        result.append("=== 搜索结果 ===\n\n");
        
        if (searchResults != null && !searchResults.isEmpty()) {
            result.append("相关网页 (").append(searchResults.size()).append(" 个):\n");
            for (int i = 0; i < searchResults.size(); i++) {
                JSONObject item = searchResults.getJSONObject(i);
                result.append(i + 1).append(". ").append(item.getStr("title", "")).append("\n");
                result.append("   描述: ").append(item.getStr("snippet", "")).append("\n");
                result.append("   链接: ").append(item.getStr("link", "")).append("\n\n");
            }
        }

        if (imageResults != null && !imageResults.isEmpty()) {
            result.append("=== 图片结果 (").append(imageResults.size()).append(" 张) ===\n\n");
            for (int i = 0; i < imageResults.size(); i++) {
                String imageUrl = imageResults.getStr(i);
                result.append("图片 ").append(i + 1).append(": ").append(imageUrl).append("\n");
            }
        } else {
            result.append("未找到相关图片");
        }

        return result.toString();
    }

    /**
     * 只返回图片链接的搜索方法
     */
    @Tool(description = "Search for image URLs only")
    public String searchImageUrls(@ToolParam(description = "Search query keyword") String query) {
        
        Map<String, Object> params = new HashMap<>();
        params.put("query", query);
        params.put("search_service", "google");
        params.put("max_results", 5);
        params.put("crawl_results", 0);
        params.put("image", true);
        params.put("language", "");

        try {
            HttpResponse response = HttpRequest.post(API_URL)
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(JSONUtil.toJsonStr(params))
                    .timeout(30000)
                    .execute();

            if (response.isOk()) {
                JSONObject json = JSONUtil.parseObj(response.body());
                var imageResults = json.getJSONArray("images");
                
                if (imageResults != null && !imageResults.isEmpty()) {
                    StringBuilder urls = new StringBuilder();
                    urls.append("找到 ").append(imageResults.size()).append(" 张图片:\n\n");
                    for (int i = 0; i < imageResults.size(); i++) {
                        urls.append(imageResults.getStr(i)).append("\n");
                    }
                    return urls.toString();
                } else {
                    return "未找到相关图片";
                }
            } else {
                return "搜索失败";
            }

        } catch (Exception e) {
            return "搜索错误: " + e.getMessage();
        }
    }
}