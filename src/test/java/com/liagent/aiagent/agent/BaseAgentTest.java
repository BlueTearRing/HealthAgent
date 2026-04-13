package com.liagent.aiagent.agent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
class BaseAgentTest {
    @Resource
    private Manus manus;

    @Test
    void run() {
        String userPrompt = "";
        String answer = manus.run(userPrompt);
        Assertions.assertNotNull(answer);
    }
}