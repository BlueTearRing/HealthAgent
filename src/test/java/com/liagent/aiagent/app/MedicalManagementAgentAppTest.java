package com.liagent.aiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MedicalManagementAgentAppTest {

    @Resource
    private MedicalManagementAgentApp medicalManagementAgentApp;

    @Test
    void doChat() {
        String message = "我最近头疼，恶心，还无力，我该吃什么调理，日常该怎么办，并给出郑州二七周边医院";
        String uuid  = UUID.randomUUID().toString();
        String s = medicalManagementAgentApp.doChat(message,uuid);
        assertNotNull(s);
    }
}