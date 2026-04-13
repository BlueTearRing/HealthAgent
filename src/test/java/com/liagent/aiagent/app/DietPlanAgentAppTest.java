package com.liagent.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DietPlanAgentAppTest {
    @Resource
    private DietPlanAgentApp app;

    @Test
    void doChat() {
        String message = "你好，我最近减肥，我明天该如何规划吃饭,请给我一些饮食图片";

        String chatId = UUID.randomUUID().toString();
        String s = app.doChat(message, chatId);
        assertNotNull(s);
    }
}