package com.liagent.aiagent.controller;

import com.liagent.aiagent.app.MedicalManagementAgentApp;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

@RestController
@RequestMapping("medical_manage_agent")
@Tag(name = "医疗问答接口")
public class MedicalController {

    @Resource
    private MedicalManagementAgentApp medicalManagementAgentApp;

    /**
     * 流式返回医疗ai的回复
     * @param message 用户消息
     * @param chatId 会话id
     * @return 返回结果
     */
    @GetMapping
    @Operation(summary = "医疗ai流式返回")
    public SseEmitter getMedical(String message,String chatId) {
        SseEmitter emitter = new SseEmitter(180000L);
        medicalManagementAgentApp.doChatFlux(message,chatId)
                .subscribe(
                        chunk->{
                            try{
                                emitter.send(chunk);
                            }catch (IOException e){
                                emitter.completeWithError(e);
                            }
                        },
                        emitter::completeWithError,
                        emitter::complete
                );

        return emitter;
    }
}
