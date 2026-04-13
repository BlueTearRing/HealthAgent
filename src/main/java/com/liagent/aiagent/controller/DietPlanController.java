package com.liagent.aiagent.controller;

import com.liagent.aiagent.app.DietPlanAgentApp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/diet_plan_agent")
@Tag(name = "饮食规划接口")
public class DietPlanController {

    @Resource
    private DietPlanAgentApp dietPlanAgentApp;

    /**
     * 流式返回结果
     * @param message 用户消息
     * @param chatId 会话·id
     * @return 返回结果
     */
    @GetMapping
    @Operation(summary = "饮食ai流式返回")
    public SseEmitter dietPlanAgent(String message,String chatId) {
        //创建一个超时时间较长的SseEmitter
        SseEmitter emitter = new SseEmitter(180000L);
        dietPlanAgentApp.doChatFlux(message, chatId)
                .subscribe(
                        //处理每条消息
                        chunk -> {
                            try {
                                emitter.send(chunk);
                            }catch (IOException e){
                                emitter.completeWithError(e);
                            }
                        },
                        //处理错误
                        emitter::completeWithError,
                        //处理完成
                        emitter::complete
                );

        return emitter;
    }
}
