package com.liagent.aiagent.controller;

import com.liagent.aiagent.agent.BaseAgent;
import com.liagent.aiagent.agent.Manus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/auto_plan_agent")
@Tag(name = "自主规划智能体接口")
public class AutonomousPlanController {

    @Resource
    private Manus manus;

    /**
     * 流式输出
     * @param message 用户消息
     * @return 流式返
     */
    @GetMapping
    @Operation(summary = "自主规划智能体")
    public SseEmitter auto_plan_agent(String message) {
        SseEmitter sseEmitter = manus.runStream(message);
        return sseEmitter;
    }
}
